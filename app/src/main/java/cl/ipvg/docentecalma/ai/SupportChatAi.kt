package cl.ipvg.docentecalma.ai

/**
 * Contrato público del servicio de chat socioemocional.
 *
 * Es lo único que el ViewModel ve de la capa `ai/`. Detrás puede haber:
 * - `GeminiSupportChatAi` (impl primaria vía Gemini),
 * - `FallbackSupportChatAi` (impl local sin red),
 * - `ResilientSupportChatAi` (envuelve a ambas y degrada si falla la primaria).
 *
 * Siempre retorna [AiResult] — nunca lanza excepciones para errores esperados.
 */
interface SupportChatAi {

    /**
     * Produce una respuesta al mensaje del usuario considerando [history]
     * como contexto previo (multi-turno). La implementación decide cuánta
     * historia usa; los llamadores pueden pasar toda la sesión.
     */
    suspend fun reply(
        history: List<SupportChatTurn>,
        userMessage: String
    ): AiResult
}
