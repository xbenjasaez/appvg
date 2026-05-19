package cl.ipvg.docentecalma.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pilot_analytics_event",
    indices = [
        Index(value = ["event_type"]),
        Index(value = ["occurred_at"])
    ]
)
data class PilotAnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** Nombre estable del evento (p. ej. `consent_accepted`). */
    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "occurred_at")
    val occurredAtEpochMs: Long,

    /**
     * Clave secundaria no sensible: p. ej. id de micromódulo del catálogo interno.
     * Sin texto libre del usuario.
     */
    @ColumnInfo(name = "secondary_key")
    val secondaryKey: String? = null,

    /** Metadato entero opcional (p. ej. bucket de longitud); nunca texto en claro. */
    @ColumnInfo(name = "int_meta")
    val intMeta: Int? = null
)
