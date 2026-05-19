package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Chequeo emocional tal como lo usa la capa de dominio y la UI.
 * No contiene anotaciones Room ni detalles de persistencia.
 */
data class EmotionalCheckIn(
    val id: Long,
    val emotion: Emotion,
    val intensity: Int,
    val note: String?,
    val createdAt: Instant
) {
    init {
        require(intensity in INTENSITY_RANGE) {
            "intensity debe estar entre ${INTENSITY_RANGE.first} y ${INTENSITY_RANGE.last}"
        }
    }

    companion object {
        val INTENSITY_RANGE: IntRange = 1..5
    }
}
