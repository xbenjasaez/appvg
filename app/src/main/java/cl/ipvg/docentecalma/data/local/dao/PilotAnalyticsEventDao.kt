package cl.ipvg.docentecalma.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.ipvg.docentecalma.data.local.entity.PilotAnalyticsEventEntity
import kotlinx.coroutines.flow.Flow

data class PilotEventCountRow(
    @ColumnInfo(name = "event_type")
    val eventType: String,
    @ColumnInfo(name = "cnt")
    val count: Int
)

data class PilotTimeBoundsRow(
    @ColumnInfo(name = "min_ts")
    val minEpochMs: Long?,
    @ColumnInfo(name = "max_ts")
    val maxEpochMs: Long?
)

@Dao
interface PilotAnalyticsEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PilotAnalyticsEventEntity): Long

    @Query(
        """
        SELECT event_type, COUNT(*) AS cnt
        FROM pilot_analytics_event
        GROUP BY event_type
        ORDER BY event_type ASC
        """
    )
    fun observeGroupedCounts(): Flow<List<PilotEventCountRow>>

    @Query("SELECT COUNT(*) FROM pilot_analytics_event")
    fun observeTotalCount(): Flow<Long>

    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT date(occurred_at / 1000, 'unixepoch', 'localtime') AS d
            FROM pilot_analytics_event
        )
        """
    )
    fun observeDistinctLocalDayCount(): Flow<Int>

    /**
     * Días locales distintos con al menos un evento en los últimos 14 días (reloj del dispositivo).
     * La ventana usa `strftime` en SQLite; se recalcula al invalidarse la tabla.
     */
    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT date(occurred_at / 1000, 'unixepoch', 'localtime') AS d
            FROM pilot_analytics_event
            WHERE occurred_at >= ((strftime('%s', 'now', 'localtime') - 1209600) * 1000)
        )
        """
    )
    fun observeDistinctLocalDayCountLast14Days(): Flow<Int>

    @Query(
        """
        SELECT MIN(occurred_at) AS min_ts, MAX(occurred_at) AS max_ts
        FROM pilot_analytics_event
        """
    )
    fun observeTimeBounds(): Flow<PilotTimeBoundsRow>

    @Query(
        """
        SELECT COUNT(*) FROM pilot_analytics_event
        WHERE event_type = :eventType AND secondary_key = :secondaryKey
        """
    )
    suspend fun countByTypeAndSecondary(eventType: String, secondaryKey: String): Int
}
