package cl.ipvg.docentecalma.ai

/**
 * Representación neutral de un turno de conversación para la capa `ai/`.
 *
 * No se usa `ChatMessage` del dominio directamente porque el módulo `ai/` no
 * debe depender de Room ni de tipos específicos de la UI. El ViewModel mapea
 * `ChatMessage` ⇄ `SupportChatTurn` en los bordes.
 */
data class SupportChatTurn(
    val role: Role,
    val text: String
) {
    enum class Role { USER, MODEL }
}
