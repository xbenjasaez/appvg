package cl.ipvg.docentecalma.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import cl.ipvg.docentecalma.data.local.dao.ChatMessageDao
import cl.ipvg.docentecalma.data.local.dao.EmotionalCheckInDao
import cl.ipvg.docentecalma.data.local.dao.MicromoduleProgressDao
import cl.ipvg.docentecalma.data.local.dao.PilotAnalyticsEventDao
import cl.ipvg.docentecalma.data.local.dao.RecommendationHistoryDao
import cl.ipvg.docentecalma.data.local.dao.SelfAssessmentDao
import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity
import cl.ipvg.docentecalma.data.local.entity.MicromoduleProgressEntity
import cl.ipvg.docentecalma.data.local.entity.PilotAnalyticsEventEntity
import cl.ipvg.docentecalma.data.local.entity.RecommendationHistoryEntity
import cl.ipvg.docentecalma.data.local.entity.SelfAssessmentEntity

@Database(
    entities = [
        EmotionalCheckInEntity::class,
        RecommendationHistoryEntity::class,
        ChatMessageEntity::class,
        SelfAssessmentEntity::class,
        MicromoduleProgressEntity::class,
        PilotAnalyticsEventEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emotionalCheckInDao(): EmotionalCheckInDao

    abstract fun recommendationHistoryDao(): RecommendationHistoryDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun selfAssessmentDao(): SelfAssessmentDao

    abstract fun micromoduleProgressDao(): MicromoduleProgressDao

    abstract fun pilotAnalyticsEventDao(): PilotAnalyticsEventDao

    /**
     * Borra todo el contenido generado por el usuario en una sola transacción.
     * No modifica preferencias de onboarding ni el seudónimo de instalación.
     * No borra eventos del piloto local (métricas agregadas anónimas).
     * El feedback breve en DataStore se elimina en el mismo flujo desde la capa de repositorio
     * que invoca a este método.
     */
    @Transaction
    open suspend fun clearAllUserHistory() {
        emotionalCheckInDao().deleteAll()
        recommendationHistoryDao().deleteAll()
        chatMessageDao().deleteAll()
        selfAssessmentDao().deleteAll()
        micromoduleProgressDao().deleteAll()
    }

    companion object {
        const val DATABASE_NAME: String = AppPersistenceIds.ROOM_DATABASE_FILE_NAME
    }
}
