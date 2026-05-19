package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "micromodule_progress")
data class MicromoduleProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "module_id")
    val moduleId: String,

    /** [cl.ipvg.docentecalma.domain.model.MicromoduleProgressState.name] */
    @ColumnInfo(name = "state")
    val state: String,

    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Long?,

    @ColumnInfo(name = "started_at")
    val startedAt: Long?,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long?
)
