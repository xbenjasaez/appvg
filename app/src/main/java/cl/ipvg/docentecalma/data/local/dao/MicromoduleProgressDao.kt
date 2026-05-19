package cl.ipvg.docentecalma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.MicromoduleProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MicromoduleProgressDao {

    @Query("SELECT * FROM micromodule_progress")
    fun observeAll(): Flow<List<MicromoduleProgressEntity>>

    @Query("SELECT * FROM micromodule_progress WHERE module_id = :moduleId LIMIT 1")
    suspend fun getByModuleId(moduleId: String): MicromoduleProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MicromoduleProgressEntity)

    @Query("DELETE FROM micromodule_progress")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM micromodule_progress")
    fun observeCount(): Flow<Int>
}
