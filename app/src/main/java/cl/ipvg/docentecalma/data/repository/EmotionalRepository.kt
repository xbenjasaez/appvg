package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.dao.EmotionCountRow
import cl.ipvg.docentecalma.data.local.dao.EmotionalCheckInDao
import cl.ipvg.docentecalma.domain.mapper.toDomain
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acceso único a chequeos emocionales. Expone dominio, nunca entities.
 */
@Singleton
class EmotionalRepository @Inject constructor(
    private val dao: EmotionalCheckInDao
) {

    fun observeAll(): Flow<List<EmotionalCheckIn>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeRange(from: Instant, to: Instant): Flow<List<EmotionalCheckIn>> =
        dao.observeRange(from.toEpochMilli(), to.toEpochMilli())
            .map { list -> list.map { it.toDomain() } }

    fun observeLatest(): Flow<EmotionalCheckIn?> =
        dao.observeLatest().map { it?.toDomain() }

    fun observeEmotionCounts(from: Instant, to: Instant): Flow<Map<Emotion, Int>> =
        dao.observeEmotionCounts(from.toEpochMilli(), to.toEpochMilli())
            .map { rows -> rows.toEmotionCountMap() }

    suspend fun getById(id: Long): EmotionalCheckIn? = dao.getById(id)?.toDomain()

    /**
     * Guarda un nuevo chequeo y retorna su id generado.
     * [createdAt] es inyectable para tests; por defecto usa la hora del reloj.
     */
    suspend fun save(
        emotion: Emotion,
        intensity: Int,
        note: String?,
        createdAt: Instant = Instant.now()
    ): Long {
        require(intensity in EmotionalCheckIn.INTENSITY_RANGE) {
            "intensity debe estar entre ${EmotionalCheckIn.INTENSITY_RANGE.first} y " +
                "${EmotionalCheckIn.INTENSITY_RANGE.last}"
        }
        val entity = cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity(
            emotionId = emotion.id,
            intensity = intensity,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = createdAt.toEpochMilli()
        )
        return dao.insert(entity)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    private fun List<EmotionCountRow>.toEmotionCountMap(): Map<Emotion, Int> =
        mapNotNull { row ->
            Emotion.fromId(row.emotionId)?.let { it to row.total }
        }.toMap()
}
