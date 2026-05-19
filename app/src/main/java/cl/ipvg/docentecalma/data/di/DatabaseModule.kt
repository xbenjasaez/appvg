package cl.ipvg.docentecalma.data.di

import android.content.Context
import androidx.room.Room
import cl.ipvg.docentecalma.data.local.AppDatabase
import cl.ipvg.docentecalma.data.local.AppDatabaseMigrations
import cl.ipvg.docentecalma.data.local.dao.ChatMessageDao
import cl.ipvg.docentecalma.data.local.dao.EmotionalCheckInDao
import cl.ipvg.docentecalma.data.local.dao.MicromoduleProgressDao
import cl.ipvg.docentecalma.data.local.dao.PilotAnalyticsEventDao
import cl.ipvg.docentecalma.data.local.dao.RecommendationHistoryDao
import cl.ipvg.docentecalma.data.local.dao.SelfAssessmentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
        .addMigrations(*AppDatabaseMigrations.ALL)
        // Último recurso si falta una Migration para una versión instalada.
        // Quitar antes de release estable una vez que todos los saltos estén cubiertos.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideEmotionalCheckInDao(db: AppDatabase): EmotionalCheckInDao =
        db.emotionalCheckInDao()

    @Provides
    fun provideRecommendationHistoryDao(db: AppDatabase): RecommendationHistoryDao =
        db.recommendationHistoryDao()

    @Provides
    fun provideChatMessageDao(db: AppDatabase): ChatMessageDao =
        db.chatMessageDao()

    @Provides
    fun provideSelfAssessmentDao(db: AppDatabase): SelfAssessmentDao =
        db.selfAssessmentDao()

    @Provides
    fun provideMicromoduleProgressDao(db: AppDatabase): MicromoduleProgressDao =
        db.micromoduleProgressDao()

    @Provides
    fun providePilotAnalyticsEventDao(db: AppDatabase): PilotAnalyticsEventDao =
        db.pilotAnalyticsEventDao()
}
