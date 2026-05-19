package cl.ipvg.docentecalma.ai

/**
 * Resultado tipado de una operación del servicio de IA.
 *
 * Nota: deliberadamente no existe `Loading`. El estado de carga es
 * responsabilidad del ViewModel (`SupportChatUiState.isGenerating`), no de la
 * capa ai/: el servicio expone `suspend`, por lo que "estar cargando" equivale
 * a tener una corrutina en vuelo.
 */
sealed interface AiResult {

    /**
     * Respuesta válida del modelo.
     * [fromFallback] indica si la respuesta viene del responder local porque
     * la llamada primaria falló. Útil para que la UI muestre un aviso discreto.
     */
    data class Success(
        val text: String,
        val fromFallback: Boolean = false
    ) : AiResult

    /**
     * Error al obtener respuesta. Se clasifica para que la UI elija el mensaje
     * correcto y decida si ofrecer reintento.
     */
    data class Error(
        val kind: ErrorKind,
        val message: String,
        val cause: Throwable? = null
    ) : AiResult

    enum class ErrorKind {
        /** Fallo de red o servidor. Reintentar tiene sentido. */
        NETWORK,

        /** Tiempo de espera agotado al contactar al servicio. Reintentar tiene sentido. */
        TIMEOUT,

        /** Límite de cuota / rate limit. Reintentar con espera. */
        RATE_LIMIT,

        /** La API respondió pero con texto vacío. Reintentar tiene sentido. */
        EMPTY,

        /** El modelo bloqueó la respuesta por filtros de seguridad. No reintentar. */
        SAFETY_BLOCKED,

        /** No hay API key configurada. Reintentar no tiene sentido hasta configurarla. */
        API_KEY_MISSING,

        /** Error desconocido. Reintentar una vez y luego degradar a fallback. */
        UNKNOWN
    }
}
