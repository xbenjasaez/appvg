package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "self_assessment",
    indices = [
        Index(value = ["created_at"])
    ]
)
data class SelfAssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "evaluation_type")
    val evaluationType: String,

    @ColumnInfo(name = "q1")
    val q1: Int,

    @ColumnInfo(name = "q2")
    val q2: Int,

    @ColumnInfo(name = "q3")
    val q3: Int,

    @ColumnInfo(name = "q4")
    val q4: Int,

    @ColumnInfo(name = "total_score")
    val totalScore: Int
)
