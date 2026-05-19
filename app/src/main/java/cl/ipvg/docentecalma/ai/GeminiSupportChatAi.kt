package cl.ipvg.docentecalma.ai

import android.util.Log
import cl.ipvg.docentecalma.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación primaria de [SupportChatAi] vía API REST de Gemini
 * ([GeminiRestClient], `generativelanguage.googleapis.com`).
 *
 * - La API key se lee solo desde [BuildConfig.GEMINI_API_KEY]; nunca se
 *   hardcodea en código. Si no existe, el servicio responde
 *   [AiResult.ErrorKind.API_KEY_MISSING] sin realizar llamadas.
 * - El modelo se inicializa de forma perezosa: no se paga costo de arranque
 *   hasta que llega el primer mensaje.
 * - Nunca lanza excepciones esperables hacia arriba: todas se mapean a
 *   [AiResult.Error] para que la UI decida cómo reaccionar.
 */
@Singleton
class GeminiSupportChatAi @Inject constructor() : SupportChatAi {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY
    private val isConfigured: Boolean = apiKey.isNotBlank()
    private val configuredHint: String = if (isConfigured) "configured" else "missing"

    private val client: GeminiRestClient? by lazy { if (isConfigured) GeminiRestClient(apiKey) else null }

    @Volatile
    private var resolvedModelName: String? = null

    private val bannedModels: MutableSet<String> = LinkedHashSet()

    override suspend fun reply(
        history: List<SupportChatTurn>,
        userMessage: String
    ): AiResult {
        val gemini = client ?: return AiResult.Error(
            kind = AiResult.ErrorKind.API_KEY_MISSING,
            message = "Falta GEMINI_API_KEY en local.properties."
        )

        if (userMessage.isBlank()) {
            return AiResult.Error(
                kind = AiResult.ErrorKind.EMPTY,
                message = "El mensaje está vacío."
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val contents = history.toGeminiContents() + GeminiContent(role = "user", text = userMessage)

                fun callWithResolvedModel(): String {
                    val modelName = resolvedModelName ?: resolveModelName(gemini).also { resolvedModelName = it }
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Gemini using model=$modelName")
                    }
                    return gemini.generateContent(
                        modelName = modelName,
                        systemInstruction = AiConfig.SYSTEM_PROMPT,
                        contents = contents
                    ).trim()
                }

                val text = run {
                    var last: Throwable? = null
                    repeat(3) { attempt ->
                        try {
                            return@run callWithResolvedModel()
                        } catch (e: GeminiHttpException) {
                            last = e
                            val current = resolvedModelName
                            val shouldSwapModel =
                                (e.code == 404 && e.body.contains("no longer available to new users", ignoreCase = true)) ||
                                    (e.code == 503 && e.body.contains("high demand", ignoreCase = true))

                            // Si el modelo dejó de estar disponible o está saturado, probar con otro (hasta 3).
                            if (shouldSwapModel) {
                                if (current != null) {
                                    synchronized(bannedModels) { bannedModels.add(current) }
                                    if (BuildConfig.DEBUG) {
                                        Log.w(
                                            TAG,
                                            "Banning model=$current due to HTTP ${e.code}; attempt=${attempt + 1}"
                                        )
                                    }
                                }
                                resolvedModelName = null
                                // continuar para resolver otro modelo
                            } else {
                                throw e
                            }
                        }
                    }
                    throw (last ?: IllegalStateException("Fallo inesperado sin excepción."))
                }.trim()

                if (text.isEmpty()) {
                    AiResult.Error(
                        kind = AiResult.ErrorKind.EMPTY,
                        message = "Respuesta vacía del modelo."
                    )
                } else {
                    AiResult.Success(text = text)
                }
            } catch (t: Throwable) {
                mapThrowable(t)
            }
        }
    }

    private fun mapThrowable(t: Throwable): AiResult.Error {
        val kind = when (t) {
            is GeminiContentBlockedException -> AiResult.ErrorKind.SAFETY_BLOCKED
            is SocketTimeoutException -> AiResult.ErrorKind.TIMEOUT
            is IOException -> AiResult.ErrorKind.NETWORK
            is GeminiHttpException -> if (t.code == 429) {
                AiResult.ErrorKind.RATE_LIMIT
            } else {
                AiResult.ErrorKind.NETWORK
            }
            else -> AiResult.ErrorKind.UNKNOWN
        }
        val safeMessage = when (t) {
            is GeminiContentBlockedException ->
                "La respuesta fue filtrada por políticas de seguridad del proveedor (${t.reason})."
            is GeminiHttpException -> {
                if (t.code == 429) {
                    val retryMs = parseRetryAfterMs(t.body)
                    if (retryMs != null) {
                        "Límite de cuota alcanzado. Reintenta en ~${(retryMs / 1000)}s o revisa tu plan/billing."
                    } else {
                        "Límite de cuota alcanzado. Revisa tu plan/billing y vuelve a intentar."
                    }
                } else {
                    t.body.ifBlank { "Error HTTP ${t.code} al consultar Gemini." }
                }
            }
            else -> t.message ?: "Error desconocido al consultar Gemini."
        }

        // Nunca registrar el cuerpo de error del proveedor ni el mensaje completo: puede ser largo o filtrarse vía logcat.
        if (BuildConfig.DEBUG) {
            Log.w(
                TAG,
                "Gemini reply failed kind=$kind apiKey=$configuredHint cause=${t::class.java.simpleName}"
            )
        }
        return AiResult.Error(
            kind = kind,
            message = safeMessage,
            cause = if (t is GeminiHttpException && t.code == 429) GeminiRateLimitException(parseRetryAfterMs(t.body), t) else t
        )
    }

    internal class GeminiRateLimitException(
        val retryAfterMs: Long?,
        val raw: GeminiHttpException
    ) : Exception(raw.message)

    private fun parseRetryAfterMs(body: String): Long? {
        // Preferir RetryInfo.retryDelay: "29s"
        runCatching {
            val marker = "\"retryDelay\""
            val idx = body.indexOf(marker)
            if (idx >= 0) {
                val after = body.substring(idx)
                val quoteIdx = after.indexOf('"', marker.length)
                if (quoteIdx >= 0) {
                    val nextQuote = after.indexOf('"', quoteIdx + 1)
                    if (nextQuote > quoteIdx) {
                        val value = after.substring(quoteIdx + 1, nextQuote) // e.g. 29s
                        val seconds = value.removeSuffix("s").toLongOrNull()
                        if (seconds != null) return seconds * 1000
                    }
                }
            }
        }
        // Fallback: "Please retry in 29.26s."
        val regex = Regex("Please retry in\\s+([0-9]+(?:\\.[0-9]+)?)s", RegexOption.IGNORE_CASE)
        val m = regex.find(body) ?: return null
        val seconds = m.groupValues.getOrNull(1)?.toDoubleOrNull() ?: return null
        return (seconds * 1000.0).toLong()
    }

    private fun resolveModelName(client: GeminiRestClient): String {
        val allModels = runCatching { client.listModelsSupportingGenerateContent() }.getOrDefault(emptyList())
        val models = synchronized(bannedModels) {
            if (bannedModels.isEmpty()) allModels else allModels.filterNot { bannedModels.contains(it) }
        }
        if (models.isEmpty()) {
            // Fallback conservador: usar el configurado localmente.
            return AiConfig.MODEL_NAME.let { if (it.startsWith("models/")) it else "models/$it" }
        }

        val preferred = listOf(
            "models/gemini-2.5-flash",
            "models/gemini-2.5-flash-lite",
            "models/gemini-2.5-pro",
            "models/gemini-1.5-flash",
            "models/gemini-1.5-pro",
            "models/gemini-pro"
        )
        preferred.firstOrNull { p -> models.any { it == p } }?.let { return it }

        // Si no hay match exacto, preferir cualquier "flash".
        models.firstOrNull { it.contains("flash", ignoreCase = true) }?.let { return it }
        return models.first()
    }

    private fun List<SupportChatTurn>.toGeminiContents(): List<GeminiContent> =
        takeLast(AiConfig.MAX_HISTORY_TURNS).map { turn ->
            val role = when (turn.role) {
                SupportChatTurn.Role.USER -> "user"
                SupportChatTurn.Role.MODEL -> "model"
            }
            GeminiContent(role = role, text = turn.text)
        }

    private companion object {
        const val TAG: String = "DocenteCalmaAi"
    }
}
