package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.local.dao.PilotAnalyticsEventDao
import cl.ipvg.docentecalma.data.local.dao.PilotEventCountRow
import cl.ipvg.docentecalma.data.local.entity.PilotAnalyticsEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class PilotMetricsRollup(
    /** Días locales distintos con al menos un evento (incluye cualquier evento ya guardado). */
    val distinctDaysWithEvents: Int,
    val firstEventEpochMs: Long?,
    val lastEventEpochMs: Long?
)

@Singleton
class PilotAnalyticsRepository @Inject constructor(
    private val dao: PilotAnalyticsEventDao
) {

    fun observeGroupedCounts(): Flow<List<PilotEventCountRow>> = dao.observeGroupedCounts()

    fun observeTotalCount(): Flow<Long> = dao.observeTotalCount()

    fun observeRollup(): Flow<PilotMetricsRollup> = combine(
        dao.observeDistinctLocalDayCount(),
        dao.observeTimeBounds()
    ) { distinctDays, bounds ->
        PilotMetricsRollup(
            distinctDaysWithEvents = distinctDays,
            firstEventEpochMs = bounds.minEpochMs,
            lastEventEpochMs = bounds.maxEpochMs
        )
    }

    /** Días con actividad registrada en la ventana móvil de 14 días (aprox. adherencia reciente). */
    fun observeDistinctLocalDayCountLast14Days(): Flow<Int> =
        dao.observeDistinctLocalDayCountLast14Days()

    /**
     * Marca el día local actual como activo como mucho una vez (evento [PilotEventNames.DAY_ACTIVE]).
     * La clave es solo la fecha (yyyy-MM-dd); no identifica a la persona.
     */
    suspend fun recordDayActiveIfNeeded(nowMs: Long = System.currentTimeMillis()) {
        val dayKey = LocalDate.ofInstant(Instant.ofEpochMilli(nowMs), ZoneId.systemDefault()).toString()
        runCatching {
            if (dao.countByTypeAndSecondary(PilotEventNames.DAY_ACTIVE, dayKey) > 0) return@runCatching
            dao.insert(
                PilotAnalyticsEventEntity(
                    eventType = PilotEventNames.DAY_ACTIVE,
                    occurredAtEpochMs = nowMs,
                    secondaryKey = dayKey,
                    intMeta = null
                )
            )
        }
    }

    /**
     * Registro best-effort: un fallo de Room no debe afectar el flujo de usuario.
     */
    suspend fun record(
        type: String,
        secondaryKey: String? = null,
        intMeta: Int? = null,
        atEpochMs: Long = System.currentTimeMillis()
    ) {
        runCatching {
            dao.insert(
                PilotAnalyticsEventEntity(
                    eventType = type,
                    occurredAtEpochMs = atEpochMs,
                    secondaryKey = secondaryKey,
                    intMeta = intMeta
                )
            )
        }
    }
}
