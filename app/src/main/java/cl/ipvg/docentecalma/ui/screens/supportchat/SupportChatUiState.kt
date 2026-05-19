package cl.ipvg.docentecalma.ui.screens.supportchat

import cl.ipvg.docentecalma.ai.AiResult
import cl.ipvg.docentecalma.domain.model.ChatMessage

/**
 * Estado UI del chat de apoyo.
 *
 * El estado distingue tres condiciones que la UI necesita tratar distinto:
 * - [isGenerating] mientras hay una corrutina esperando al servicio de IA.
 * - [lastReplyFromFallback] cuando la última respuesta no vino del modelo
 *   sino del responder local (por fallo o sin API key). Permite un aviso discreto.
 * - [error] cuando la llamada falló y no se logró escribir respuesta;
 *   conserva el texto del usuario en [retryableInput] para ofrecer reintento.
 */
data class SupportChatUiState(
    val sessionId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isInitializing: Boolean = true,
    val isGenerating: Boolean = false,
    val lastReplyFromFallback: Boolean = false,
    val error: ChatError? = null
) {
    val canSend: Boolean
        get() = sessionId.isNotBlank() && input.isNotBlank() && !isGenerating

    val showEmpty: Boolean
        get() = !isInitializing && messages.isEmpty() && error == null

    val canRetry: Boolean
        get() = error?.retryableInput != null && !isGenerating

    companion object {
        const val INPUT_MAX_LENGTH: Int = 1_000
    }
}

/**
 * Error tipado del chat, listo para pintarse sin que la UI conozca
 * los detalles de la capa ai/.
 */
data class ChatError(
    val kind: AiResult.ErrorKind,
    val message: String,
    val retryableInput: String? = null,
    /** Texto auxiliar breve (p. ej. tras bloqueo o timeout); no sustituye a [message]. */
    val hint: String? = null
)

sealed interface SupportChatEvent {
    data class OnInputChanged(val text: String) : SupportChatEvent
    data object OnSend : SupportChatEvent
    data object OnRetry : SupportChatEvent
    data object OnClearSession : SupportChatEvent
    data object DismissError : SupportChatEvent
    data object DismissFallbackNotice : SupportChatEvent
}
