package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro histórico de una recomendación mostrada o aplicada tras un chequeo.
 *
 * [checkInId] es una referencia lógica a `emotional_check_in.id`. No se usa
 * `ForeignKey` intencionalmente para permitir conservar el histórico aunque
 * el check-in se elimine manualmente (auditoría del dominio).
 */
@Entity(
    tableName = "recommendation_history",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["check_in_id"]),
        Index(value = ["emotion_id"])
    ]
)
data class RecommendationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "check_in_id")
    val checkInId: Long?,

    @ColumnInfo(name = "emotion_id")
    val emotionId: String,

    @ColumnInfo(name = "intensity")
    val intensity: Int,

    @ColumnInfo(name = "type_id")
    val typeId: String,

    @ColumnInfo(name = "summary")
    val summary: String,

    @ColumnInfo(name = "acknowledged")
    val acknowledged: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
