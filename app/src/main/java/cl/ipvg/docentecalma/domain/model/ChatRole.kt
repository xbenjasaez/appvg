package cl.ipvg.docentecalma.domain.model

/**
 * Rol de un mensaje en el chat de apoyo. El [id] es persistido en Room.
 */
enum class ChatRole(val id: String) {
    USER("user"),
    MODEL("model");

    companion object {
        fun fromId(id: String): ChatRole =
            entries.firstOrNull { it.id == id } ?: USER
    }
}
