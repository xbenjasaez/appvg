package cl.ipvg.docentecalma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.SelfAssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SelfAssessmentDao {

    @Query("SELECT * FROM self_assessment ORDER BY created_at DESC")
    fun observeAll(): Flow<List<SelfAssessmentEntity>>

    @Query(
        "SELECT * FROM self_assessment ORDER BY created_at DESC LIMIT 1"
    )
    suspend fun getLatest(): SelfAssessmentEntity?

    @Query("SELECT * FROM self_assessment WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SelfAssessmentEntity?

    @Query("SELECT COUNT(*) FROM self_assessment")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: SelfAssessmentEntity): Long

    @Query("DELETE FROM self_assessment WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM self_assessment")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM self_assessment")
    fun observeCount(): Flow<Int>
}
