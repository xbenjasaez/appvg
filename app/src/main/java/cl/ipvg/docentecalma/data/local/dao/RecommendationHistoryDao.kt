package cl.ipvg.docentecalma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.RecommendationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationHistoryDao {

    @Query("SELECT * FROM recommendation_history ORDER BY created_at DESC")
    fun observeAll(): Flow<List<RecommendationHistoryEntity>>

    @Query(
        "SELECT * FROM recommendation_history " +
            "WHERE check_in_id = :checkInId " +
            "ORDER BY created_at DESC"
    )
    fun observeForCheckIn(checkInId: Long): Flow<List<RecommendationHistoryEntity>>

    @Query("SELECT * FROM recommendation_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RecommendationHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: RecommendationHistoryEntity): Long

    @Query(
        "UPDATE recommendation_history " +
            "SET acknowledged = :acknowledged " +
            "WHERE id = :id"
    )
    suspend fun setAcknowledged(id: Long, acknowledged: Boolean)

    @Query("DELETE FROM recommendation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recommendation_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM recommendation_history")
    fun observeCount(): Flow<Int>
}
