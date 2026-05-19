package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mensaje persistido del chat de apoyo.
 *
 * Versión simplificada: no hay tabla aparte de sesión. El agrupamiento se hace
 * por [sessionId], que también permite borrar una conversación completa sin
 * tocar el resto del historial.
 */
@Entity(
    tableName = "chat_message",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["session_id", "created_at"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "role_id")
    val roleId: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
