package cl.ipvg.docentecalma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmotionalCheckInDao {

    @Query("SELECT * FROM emotional_check_in ORDER BY created_at DESC")
    fun observeAll(): Flow<List<EmotionalCheckInEntity>>

    @Query(
        "SELECT * FROM emotional_check_in " +
            "WHERE created_at BETWEEN :from AND :to " +
            "ORDER BY created_at DESC"
    )
    fun observeRange(from: Long, to: Long): Flow<List<EmotionalCheckInEntity>>

    @Query("SELECT * FROM emotional_check_in WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmotionalCheckInEntity?

    @Query(
        "SELECT * FROM emotional_check_in " +
            "ORDER BY created_at DESC LIMIT 1"
    )
    fun observeLatest(): Flow<EmotionalCheckInEntity?>

    @Query(
        "SELECT emotion_id AS emotionId, COUNT(*) AS total " +
            "FROM emotional_check_in " +
            "WHERE created_at BETWEEN :from AND :to " +
            "GROUP BY emotion_id"
    )
    fun observeEmotionCounts(from: Long, to: Long): Flow<List<EmotionCountRow>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EmotionalCheckInEntity): Long

    @Query("DELETE FROM emotional_check_in WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM emotional_check_in")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM emotional_check_in")
    fun observeCount(): Flow<Int>
}

/**
 * Fila agregada usada por la pantalla de progreso.
 * No tiene anotaciones Room de tabla; solo se materializa en consultas.
 */
data class EmotionCountRow(
    val emotionId: String,
    val total: Int
)
