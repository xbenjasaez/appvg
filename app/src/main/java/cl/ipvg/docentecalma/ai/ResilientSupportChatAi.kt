package cl.ipvg.docentecalma.ai

import android.util.Log
import cl.ipvg.docentecalma.BuildConfig
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Composición que hace resiliente y responsable al chat:
 *
 * 1. Pre-clasifica el mensaje con [RiskClassifier]. Si detecta una categoría
 *    de riesgo (autolesión, daño a terceros, violencia o crisis aguda),
 *    responde con el mensaje seguro correspondiente y NO consulta al modelo.
 *    Esta política es explícita de la app y no depende de los filtros del
 *    proveedor.
 * 2. Reintenta una vez el servicio primario ante errores transitorios (red,
 *    tiempo de espera, respuesta vacía, etc.). No reintenta ante bloqueo de
 *    seguridad del proveedor ni API key faltante.
 * 3. Si el primario sigue fallando, delega en [FallbackSupportChatAi], salvo
 *    cuando el error es [AiResult.ErrorKind.SAFETY_BLOCKED]: en ese caso se
 *    devuelve el error para no sustituir el bloqueo por una respuesta local
 *    que podría confundir a la persona usuaria.
 *
 * El ViewModel siempre consume esta clase — no distingue entre Gemini, reglas
 * locales o respuestas de derivación. El flag [AiResult.Success.fromFallback]
 * permite a la UI mostrar un aviso discreto cuando la respuesta no vino del
 * modelo.
 */
@Singleton
class ResilientSupportChatAi @Inject constructor(
    private val primary: GeminiSupportChatAi,
    private val fallback: FallbackSupportChatAi
) : SupportChatAi {

    override suspend fun reply(
        history: List<SupportChatTurn>,
        userMessage: String
    ): AiResult {
        val risk = RiskClassifier.categorize(userMessage)
        if (risk != RiskCategory.NONE) {
            return AiResult.Success(
                text = RiskClassifier.safetyResponseFor(risk),
                fromFallback = false
            )
        }

        val first = primary.reply(history, userMessage)
        if (first is AiResult.Success) return first

        val firstError = first as AiResult.Error
        if (firstError.kind == AiResult.ErrorKind.SAFETY_BLOCKED) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Primary blocked by safety; not falling back to local responder.")
            }
            return firstError
        }

        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Primary Gemini failed kind=${firstError.kind}; will evaluate retry/fallback.")
        }
        val shouldRetry = when (firstError.kind) {
            AiResult.ErrorKind.NETWORK,
            AiResult.ErrorKind.TIMEOUT,
            AiResult.ErrorKind.RATE_LIMIT,
            AiResult.ErrorKind.EMPTY,
            AiResult.ErrorKind.UNKNOWN -> true
            AiResult.ErrorKind.SAFETY_BLOCKED,
            AiResult.ErrorKind.API_KEY_MISSING -> false
        }

        if (shouldRetry) {
            if (firstError.kind == AiResult.ErrorKind.RATE_LIMIT) {
                val retryAfterMs =
                    (firstError.cause as? GeminiSupportChatAi.GeminiRateLimitException)?.retryAfterMs
                if (retryAfterMs != null && retryAfterMs > 0) {
                    delay(retryAfterMs.coerceAtMost(60_000))
                } else {
                    delay(1_000)
                }
            } else if (firstError.kind == AiResult.ErrorKind.TIMEOUT) {
                delay(1_000)
            }
            val second = primary.reply(history, userMessage)
            if (second is AiResult.Success) return second
            val secondErr = second as AiResult.Error
            if (secondErr.kind == AiResult.ErrorKind.SAFETY_BLOCKED) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Retry blocked by safety; not falling back to local responder.")
                }
                return secondErr
            }
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Retry Gemini failed kind=${secondErr.kind}; falling back.")
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Not retrying kind=${firstError.kind}; falling back.")
            }
        }

        return fallback.reply(history, userMessage)
    }

    private companion object {
        const val TAG: String = "DocenteCalmaAi"
    }
}
