package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.dao.RecommendationHistoryDao
import cl.ipvg.docentecalma.data.local.entity.RecommendationHistoryEntity
import cl.ipvg.docentecalma.domain.mapper.toDomain
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.RecommendationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registro histórico de recomendaciones entregadas al usuario.
 */
@Singleton
class RecommendationHistoryRepository @Inject constructor(
    private val dao: RecommendationHistoryDao
) {

    fun observeAll(): Flow<List<RecommendationHistory>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeForCheckIn(checkInId: Long): Flow<List<RecommendationHistory>> =
        dao.observeForCheckIn(checkInId).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): RecommendationHistory? = dao.getById(id)?.toDomain()

    suspend fun log(
        checkInId: Long?,
        emotion: Emotion,
        intensity: Int,
        type: RecommendationType,
        summary: String,
        acknowledged: Boolean = false,
        createdAt: Instant = Instant.now()
    ): Long {
        val entity = RecommendationHistoryEntity(
            checkInId = checkInId,
            emotionId = emotion.id,
            intensity = intensity,
            typeId = type.id,
            summary = summary,
            acknowledged = acknowledged,
            createdAt = createdAt.toEpochMilli()
        )
        return dao.insert(entity)
    }

    suspend fun setAcknowledged(id: Long, acknowledged: Boolean) =
        dao.setAcknowledged(id, acknowledged)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
