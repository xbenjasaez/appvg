package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import java.time.Instant

fun EmotionalCheckInEntity.toDomain(): EmotionalCheckIn = EmotionalCheckIn(
    id = id,
    emotion = Emotion.fromIdOrThrow(emotionId),
    intensity = intensity,
    note = note,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun EmotionalCheckIn.toEntity(): EmotionalCheckInEntity = EmotionalCheckInEntity(
    id = id,
    emotionId = emotion.id,
    intensity = intensity,
    note = note,
    createdAt = createdAt.toEpochMilli()
)
