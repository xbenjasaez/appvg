package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.RecommendationHistoryEntity
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.RecommendationType
import java.time.Instant

fun RecommendationHistoryEntity.toDomain(): RecommendationHistory = RecommendationHistory(
    id = id,
    checkInId = checkInId,
    emotion = Emotion.fromIdOrThrow(emotionId),
    intensity = intensity,
    type = RecommendationType.fromId(typeId),
    summary = summary,
    acknowledged = acknowledged,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun RecommendationHistory.toEntity(): RecommendationHistoryEntity = RecommendationHistoryEntity(
    id = id,
    checkInId = checkInId,
    emotionId = emotion.id,
    intensity = intensity,
    typeId = type.id,
    summary = summary,
    acknowledged = acknowledged,
    createdAt = createdAt.toEpochMilli()
)
