package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Registro histórico de una recomendación mostrada o aplicada.
 * Asociado opcionalmente a un [EmotionalCheckIn] mediante [checkInId].
 */
data class RecommendationHistory(
    val id: Long,
    val checkInId: Long?,
    val emotion: Emotion,
    val intensity: Int,
    val type: RecommendationType,
    val summary: String,
    val acknowledged: Boolean,
    val createdAt: Instant
)
