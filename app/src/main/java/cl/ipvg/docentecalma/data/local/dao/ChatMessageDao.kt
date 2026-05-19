package cl.ipvg.docentecalma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query(
        "SELECT * FROM chat_message " +
            "WHERE session_id = :sessionId " +
            "ORDER BY created_at ASC"
    )
    fun observeSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query(
        "SELECT session_id FROM chat_message " +
            "GROUP BY session_id " +
            "ORDER BY MAX(created_at) DESC"
    )
    fun observeSessionIds(): Flow<List<String>>

    /**
     * Resumen agregado por sesión. Orden: sesiones más recientes primero.
     * [ChatSessionSummaryRow.firstUserContent] puede ser null si la sesión
     * comienza con un mensaje del modelo (no ocurre hoy, pero se protege).
     */
    @Query(
        "SELECT " +
            "c.session_id AS sessionId, " +
            "COUNT(*) AS messageCount, " +
            "MIN(c.created_at) AS firstCreatedAt, " +
            "MAX(c.created_at) AS lastCreatedAt, " +
            "(" +
            "  SELECT content FROM chat_message " +
            "  WHERE session_id = c.session_id AND role_id = 'user' " +
            "  ORDER BY created_at ASC LIMIT 1" +
            ") AS firstUserContent " +
            "FROM chat_message c " +
            "GROUP BY c.session_id " +
            "ORDER BY lastCreatedAt DESC"
    )
    fun observeSessionSummaries(): Flow<List<ChatSessionSummaryRow>>

    @Query("SELECT * FROM chat_message WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ChatMessageEntity): Long

    @Query("DELETE FROM chat_message WHERE session_id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_message")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM chat_message")
    fun observeCount(): Flow<Int>
}

/**
 * Fila agregada usada por la pantalla de historial.
 * No tiene anotaciones Room de tabla: solo se materializa en consultas.
 */
data class ChatSessionSummaryRow(
    val sessionId: String,
    val messageCount: Int,
    val firstCreatedAt: Long,
    val lastCreatedAt: Long,
    val firstUserContent: String?
)
