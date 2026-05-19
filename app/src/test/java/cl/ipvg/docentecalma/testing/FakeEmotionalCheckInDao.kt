package cl.ipvg.docentecalma.testing

import cl.ipvg.docentecalma.data.local.dao.EmotionCountRow
import cl.ipvg.docentecalma.data.local.dao.EmotionalCheckInDao
import cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [EmotionalCheckInDao] para tests unitarios.
 *
 * Mantiene los datos en un [MutableStateFlow], por lo que las lecturas reactivas
 * se comportan como en Room (emiten tras cada escritura). Pensado para pruebas
 * locales: no simula constraints ni índices reales.
 */
class FakeEmotionalCheckInDao(
    initial: List<EmotionalCheckInEntity> = emptyList()
) : EmotionalCheckInDao {

    private val items = MutableStateFlow(initial)
    private var nextId: Long = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    fun currentItems(): List<EmotionalCheckInEntity> = items.value

    override fun observeAll(): Flow<List<EmotionalCheckInEntity>> =
        items.map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeRange(from: Long, to: Long): Flow<List<EmotionalCheckInEntity>> =
        items.map { list ->
            list.filter { it.createdAt in from..to }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun getById(id: Long): EmotionalCheckInEntity? =
        items.value.firstOrNull { it.id == id }

    override fun observeLatest(): Flow<EmotionalCheckInEntity?> =
        items.map { list -> list.maxByOrNull { it.createdAt } }

    override fun observeEmotionCounts(from: Long, to: Long): Flow<List<EmotionCountRow>> =
        items.map { list ->
            list.filter { it.createdAt in from..to }
                .groupingBy { it.emotionId }
                .eachCount()
                .map { (emotionId, count) -> EmotionCountRow(emotionId, count) }
        }

    override suspend fun insert(entity: EmotionalCheckInEntity): Long {
        val assigned = if (entity.id == 0L) nextId++ else entity.id
        items.value = items.value + entity.copy(id = assigned)
        return assigned
    }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }

    override fun observeCount(): Flow<Int> = items.map { it.size }
}
