package cl.ipvg.docentecalma.ui.screens.supportchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.ai.AiResult
import cl.ipvg.docentecalma.ai.RiskCategory
import cl.ipvg.docentecalma.ai.RiskClassifier
import cl.ipvg.docentecalma.ai.SupportChatAi
import cl.ipvg.docentecalma.ai.SupportChatTurn
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.analytics.PilotFlowDurationBuckets
import cl.ipvg.docentecalma.data.analytics.PilotFlowSecondaryKeys
import cl.ipvg.docentecalma.data.analytics.PilotMessageLengthBuckets
import cl.ipvg.docentecalma.data.repository.ChatRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.safety.RiskEvent
import cl.ipvg.docentecalma.safety.RiskEventFactory
import cl.ipvg.docentecalma.safety.RiskEventSink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * ViewModel del chat de apoyo.
 *
 * Orquesta tres piezas:
 * - [ChatRepository] como fuente de verdad del historial (Room, reactivo).
 * - [SupportChatAi] como única dependencia hacia la capa `ai/`.
 * - `SupportChatUiState` como proyección observada por la pantalla.
 *
 * Reglas clave:
 * - El mensaje del usuario se persiste SIEMPRE antes de llamar al servicio.
 * - Si la llamada falla, el mensaje del usuario queda visible y el input se
 *   preserva en [SupportChatUiState.error.retryableInput] para permitir retry.
 * - Si el resultado viene del fallback, se marca
 *   [SupportChatUiState.lastReplyFromFallback] para que la UI avise
 *   discretamente.
 *
 * Nota de diseño: el estado se mantiene en un [MutableStateFlow] único
 * (no como `combine` con `WhileSubscribed`) para que toda la lógica interna
 * lea valores siempre consistentes sin depender de si la UI está suscrita.
 */
@HiltViewModel
class SupportChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val supportChatAi: SupportChatAi,
    private val riskEventFactory: RiskEventFactory,
    private val riskEventSink: RiskEventSink,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportChatUiState())
    val uiState: StateFlow<SupportChatUiState> = _uiState.asStateFlow()

    private var messagesJob: Job? = null

    private val flowStartedAtMs: Long = System.currentTimeMillis()

    init {
        startSession()
    }

    fun onEvent(event: SupportChatEvent) {
        when (event) {
            is SupportChatEvent.OnInputChanged -> _uiState.update {
                it.copy(
                    input = event.text.take(SupportChatUiState.INPUT_MAX_LENGTH),
                    error = null
                )
            }
            SupportChatEvent.OnSend -> sendCurrentInput()
            SupportChatEvent.OnRetry -> retryLastFailed()
            SupportChatEvent.OnClearSession -> clearSession()
            SupportChatEvent.DismissError -> _uiState.update { it.copy(error = null) }
            SupportChatEvent.DismissFallbackNotice -> _uiState.update {
                it.copy(lastReplyFromFallback = false)
            }
        }
    }

    private fun startSession() {
        val sessionId = chatRepository.newSessionId()
        _uiState.update {
            it.copy(
                sessionId = sessionId,
                messages = emptyList(),
                isInitializing = true
            )
        }
        observeSessionMessages(sessionId)
        viewModelScope.launch {
            pilotAnalyticsRepository.record(PilotEventNames.CHAT_OPENED)
        }
    }

    /**
     * Observa la sesión actual en Room y sincroniza `messages` en `_uiState`.
     * Cancela el job previo para evitar duplicación al cambiar de sesión.
     */
    private fun observeSessionMessages(sessionId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.observeSession(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages, isInitializing = false) }
            }
        }
    }

    private fun sendCurrentInput() {
        val current = _uiState.value
        if (!current.canSend) return
        val content = current.input.trim()
        _uiState.update { it.copy(input = "") }
        dispatchMessage(sessionId = current.sessionId, userText = content)
    }

    private fun retryLastFailed() {
        val current = _uiState.value
        val retryInput = current.error?.retryableInput ?: return
        if (current.isGenerating || current.sessionId.isBlank()) return
        _uiState.update { it.copy(error = null) }
        dispatchMessage(
            sessionId = current.sessionId,
            userText = retryInput,
            persistUser = false
        )
    }

    /**
     * Flujo común para enviar un mensaje al asistente:
     * 1. Persiste el turno USER (salvo en retry donde ya está persistido).
     * 2. Construye historial y llama a `supportChatAi.reply`.
     * 3. Interpreta el [AiResult] y actualiza estado/persistencia.
     */
    private fun dispatchMessage(
        sessionId: String,
        userText: String,
        persistUser: Boolean = true
    ) {
        _uiState.update {
            it.copy(
                isGenerating = true,
                error = null,
                lastReplyFromFallback = false
            )
        }
        viewModelScope.launch {
            emitRiskEventIfNeeded(sessionId = sessionId, userText = userText)
            if (persistUser) {
                runCatching {
                    chatRepository.append(sessionId, ChatRole.USER, userText)
                }.onFailure { t ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = ChatError(
                                kind = AiResult.ErrorKind.UNKNOWN,
                                message = t.message
                                    ?: "No pudimos guardar tu mensaje. Prueba de nuevo en un momento.",
                                retryableInput = userText,
                                hint = "Si se repite, reinicia la conversación desde el menú."
                            )
                        )
                    }
                    return@launch
                }
                pilotAnalyticsRepository.record(
                    PilotEventNames.CHAT_MESSAGE_SENT,
                    intMeta = PilotMessageLengthBuckets.bucket(userText.length)
                )
            }

            val history = buildHistory(excludeLastUserText = userText)
            val result = supportChatAi.reply(history = history, userMessage = userText)

            when (result) {
                is AiResult.Success -> {
                    runCatching {
                        chatRepository.append(sessionId, ChatRole.MODEL, result.text)
                    }
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = null,
                            lastReplyFromFallback = result.fromFallback
                        )
                    }
                }
                is AiResult.Error -> {
                    if (result.kind == AiResult.ErrorKind.SAFETY_BLOCKED) {
                        pilotAnalyticsRepository.record(PilotEventNames.CHAT_BLOCKED_BY_SAFETY)
                    }
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = ChatError(
                                kind = result.kind,
                                message = humanMessage(result),
                                retryableInput = if (result.kind.isRetryable()) userText else null,
                                hint = errorHint(result.kind)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun clearSession() {
        val oldSession = _uiState.value.sessionId
        val newSession = chatRepository.newSessionId()
        _uiState.update {
            it.copy(
                sessionId = newSession,
                messages = emptyList(),
                input = "",
                isGenerating = false,
                error = null,
                lastReplyFromFallback = false,
                isInitializing = false
            )
        }
        observeSessionMessages(newSession)
        if (oldSession.isNotBlank()) {
            viewModelScope.launch {
                runCatching { chatRepository.clearSession(oldSession) }
            }
        }
    }

    /**
     * Historial para multi-turno. Mapea `ChatMessage` de dominio a
     * `SupportChatTurn` del módulo ai/. Se excluye una eventual cola que
     * coincida con el mensaje que estamos por enviar para evitar que aparezca
     * dos veces si el Flow ya lo incluyó por carrera.
     */
    private fun buildHistory(excludeLastUserText: String): List<SupportChatTurn> {
        val messages = _uiState.value.messages
        val trimmed = if (
            messages.isNotEmpty() &&
            messages.last().role == ChatRole.USER &&
            messages.last().content == excludeLastUserText
        ) {
            messages.dropLast(1)
        } else {
            messages
        }
        return trimmed.map { msg ->
            SupportChatTurn(
                role = when (msg.role) {
                    ChatRole.USER -> SupportChatTurn.Role.USER
                    ChatRole.MODEL -> SupportChatTurn.Role.MODEL
                },
                text = msg.content
            )
        }
    }

    /**
     * Si el mensaje del usuario coincide con una categoría de riesgo, emite un
     * [RiskEvent] mínimo (sin texto en claro) al [RiskEventSink]. Esta llamada
     * es best-effort: cualquier fallo se traga para no afectar el flujo de
     * conversación de la persona docente.
     */
    private suspend fun emitRiskEventIfNeeded(sessionId: String, userText: String) {
        val category = RiskClassifier.categorize(userText)
        if (category == RiskCategory.NONE) return
        runCatching {
            val event = riskEventFactory.create(
                category = category,
                userText = userText,
                sessionRef = sessionId,
                source = RiskEvent.Source.SUPPORT_CHAT
            )
            riskEventSink.emit(event)
        }
    }

    private fun humanMessage(error: AiResult.Error): String = when (error.kind) {
        AiResult.ErrorKind.NETWORK ->
            "No pudimos conectar con el asistente. Revisa tu conexión e inténtalo otra vez."
        AiResult.ErrorKind.TIMEOUT ->
            "La respuesta tardó más de lo esperado y se cortó la espera. Puedes intentarlo de nuevo."
        AiResult.ErrorKind.RATE_LIMIT ->
            "El servicio está muy solicitado ahora. Espera un poco y vuelve a intentar."
        AiResult.ErrorKind.EMPTY ->
            "El asistente no devolvió una respuesta clara. Prueba con una frase un poco distinta."
        AiResult.ErrorKind.SAFETY_BLOCKED ->
            "Por políticas automáticas de seguridad no podemos generar una respuesta a eso. " +
                "No es un juicio sobre ti: a veces basta con reformular o usar menos detalle personal."
        AiResult.ErrorKind.API_KEY_MISSING ->
            "El asistente en línea no está configurado en este dispositivo; seguirás con respuestas locales de la app."
        AiResult.ErrorKind.UNKNOWN ->
            error.message.ifBlank {
                "Algo salió mal al consultar al asistente. Puedes intentar de nuevo."
            }
    }

    private fun errorHint(kind: AiResult.ErrorKind): String? = when (kind) {
        AiResult.ErrorKind.SAFETY_BLOCKED ->
            "Si quieres seguir conversando, prueba una versión más general del tema o usa los ejercicios de la app."
        AiResult.ErrorKind.TIMEOUT ->
            "Si pasa seguido, comprueba la red o espera unos segundos entre intentos."
        AiResult.ErrorKind.NETWORK ->
            "Comprueba Wi‑Fi o datos móviles."
        AiResult.ErrorKind.RATE_LIMIT ->
            "Suele normalizarse en pocos minutos."
        AiResult.ErrorKind.EMPTY ->
            "A veces ayuda acortar la pregunta o dividirla en dos mensajes."
        AiResult.ErrorKind.API_KEY_MISSING,
        AiResult.ErrorKind.UNKNOWN -> null
    }

    private fun AiResult.ErrorKind.isRetryable(): Boolean = when (this) {
        AiResult.ErrorKind.NETWORK,
        AiResult.ErrorKind.TIMEOUT,
        AiResult.ErrorKind.RATE_LIMIT,
        AiResult.ErrorKind.EMPTY,
        AiResult.ErrorKind.UNKNOWN -> true
        AiResult.ErrorKind.SAFETY_BLOCKED,
        AiResult.ErrorKind.API_KEY_MISSING -> false
    }

    override fun onCleared() {
        super.onCleared()
        val seconds = ((System.currentTimeMillis() - flowStartedAtMs) / 1000).toInt().coerceAtLeast(0)
        val bucket = PilotFlowDurationBuckets.bucket(seconds)
        runBlocking {
            pilotAnalyticsRepository.record(
                PilotEventNames.FLOW_DURATION_BUCKET,
                secondaryKey = PilotFlowSecondaryKeys.SUPPORT_CHAT,
                intMeta = bucket
            )
        }
    }
}
