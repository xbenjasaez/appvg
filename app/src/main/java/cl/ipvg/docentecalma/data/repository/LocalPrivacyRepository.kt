package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.AppDatabase
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackRepository
import cl.ipvg.docentecalma.safety.LocalRiskEventSink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resumen de filas guardadas localmente (solo conteos; no expone contenido).
 */
data class LocalDataCounts(
    val emotionalCheckIns: Int,
    val recommendations: Int,
    val chatMessages: Int,
    val selfAssessments: Int = 0,
    val micromoduleProgressRows: Int = 0,
    val hasPostUseFeedback: Boolean = false
) {
    val hasAnyStoredHistory: Boolean
        get() = emotionalCheckIns > 0 || recommendations > 0 || chatMessages > 0 ||
            selfAssessments > 0 || micromoduleProgressRows > 0 || hasPostUseFeedback
}

/**
 * Encapsula lectura de conteos y borrado transaccional del historial de usuario.
 */
@Singleton
class LocalPrivacyRepository @Inject constructor(
    private val database: AppDatabase,
    private val postUseFeedbackRepository: PostUseFeedbackRepository,
    private val riskEventSink: LocalRiskEventSink
) {

    fun observeLocalDataCounts(): Flow<LocalDataCounts> {
        val roomCounts = combine(
            database.emotionalCheckInDao().observeCount(),
            database.recommendationHistoryDao().observeCount(),
            database.chatMessageDao().observeCount(),
            database.selfAssessmentDao().observeCount(),
            database.micromoduleProgressDao().observeCount()
        ) { checkIns, recommendations, messages, selfAssessments, micromoduleRows ->
            LocalDataCounts(
                emotionalCheckIns = checkIns,
                recommendations = recommendations,
                chatMessages = messages,
                selfAssessments = selfAssessments,
                micromoduleProgressRows = micromoduleRows,
                hasPostUseFeedback = false
            )
        }
        return combine(roomCounts, postUseFeedbackRepository.hasStoredFeedback) { base, hasFeedback ->
            base.copy(hasPostUseFeedback = hasFeedback)
        }
    }

    suspend fun clearAllUserHistory() {
        database.clearAllUserHistory()
        postUseFeedbackRepository.clearStoredFeedback()
        riskEventSink.clearBufferedEvents()
    }
}

