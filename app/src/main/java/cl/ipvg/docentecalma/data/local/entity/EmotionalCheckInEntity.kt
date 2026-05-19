package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representación persistida de un chequeo emocional.
 *
 * - [emotionId] es el id estable del enum `Emotion` (no el ordinal).
 * - [intensity] está acotado en dominio al rango 1..5.
 * - [createdAt] se guarda como epoch millis; el mapping a `Instant` lo hace el mapper.
 *
 * Índices:
 * - `created_at` para listados ordenados por fecha y consultas por rango.
 * - `emotion_id` para agregados y filtros por emoción (pantalla de progreso).
 */
@Entity(
    tableName = "emotional_check_in",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["emotion_id"])
    ]
)
data class EmotionalCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "emotion_id")
    val emotionId: String,

    @ColumnInfo(name = "intensity")
    val intensity: Int,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
