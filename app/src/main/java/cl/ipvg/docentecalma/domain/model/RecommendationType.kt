package cl.ipvg.docentecalma.domain.model

/**
 * Tipo de recomendación que se muestra o registra. El [id] es persistido en Room.
 */
enum class RecommendationType(val id: String) {
    IMMEDIATE("immediate"),
    EXERCISE("exercise"),
    CHAT("chat"),
    PROFESSIONAL("professional");

    companion object {
        fun fromId(id: String): RecommendationType =
            entries.firstOrNull { it.id == id } ?: IMMEDIATE
    }
}
