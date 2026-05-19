package cl.ipvg.docentecalma.testing

import cl.ipvg.docentecalma.data.local.dao.PilotAnalyticsEventDao
import cl.ipvg.docentecalma.data.local.dao.PilotEventCountRow
import cl.ipvg.docentecalma.data.local.dao.PilotTimeBoundsRow
import cl.ipvg.docentecalma.data.local.entity.PilotAnalyticsEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class FakePilotAnalyticsEventDao : PilotAnalyticsEventDao {

    private val items = MutableStateFlow<List<PilotAnalyticsEventEntity>>(emptyList())

    override suspend fun insert(entity: PilotAnalyticsEventEntity): Long {
        val id = (items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val withId = entity.copy(id = id)
        items.value = items.value + withId
        return id
    }

    override fun observeGroupedCounts(): Flow<List<PilotEventCountRow>> =
        items.map { list ->
            list.groupingBy { it.eventType }.eachCount()
                .map { (type, cnt) -> PilotEventCountRow(eventType = type, count = cnt) }
                .sortedBy { it.eventType }
        }

    override fun observeTotalCount(): Flow<Long> =
        items.map { it.size.toLong() }

    override fun observeDistinctLocalDayCount(): Flow<Int> =
        items.map { list ->
            list.map { e ->
                Instant.ofEpochMilli(e.occurredAtEpochMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.distinct().size
        }

    override fun observeDistinctLocalDayCountLast14Days(): Flow<Int> =
        items.map { list ->
            val cutoff = Instant.now().minus(14, ChronoUnit.DAYS)
            list.filter { Instant.ofEpochMilli(it.occurredAtEpochMs).isAfter(cutoff) }
                .map { e ->
                    Instant.ofEpochMilli(e.occurredAtEpochMs)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                .distinct()
                .size
        }

    override fun observeTimeBounds(): Flow<PilotTimeBoundsRow> =
        items.map { list ->
            if (list.isEmpty()) {
                PilotTimeBoundsRow(minEpochMs = null, maxEpochMs = null)
            } else {
                PilotTimeBoundsRow(
                    minEpochMs = list.minOf { it.occurredAtEpochMs },
                    maxEpochMs = list.maxOf { it.occurredAtEpochMs }
                )
            }
        }

    override suspend fun countByTypeAndSecondary(eventType: String, secondaryKey: String): Int =
        items.value.count { it.eventType == eventType && it.secondaryKey == secondaryKey }
}
