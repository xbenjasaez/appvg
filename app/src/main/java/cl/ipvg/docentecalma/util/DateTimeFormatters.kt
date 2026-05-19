package cl.ipvg.docentecalma.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Utilidades de formato de fecha/hora para la UI.
 * Usa la zona horaria del sistema y locale es-CL.
 */
object DateTimeFormatters {

    private val LOCALE = Locale("es", "CL")

    private val FULL: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", LOCALE)
            .withZone(ZoneId.systemDefault())

    private val SHORT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM HH:mm", LOCALE)
            .withZone(ZoneId.systemDefault())

    fun full(instant: Instant): String = FULL.format(instant)

    fun short(instant: Instant): String = SHORT.format(instant)

    fun relative(instant: Instant, now: Instant = Instant.now()): String {
        val minutes = ChronoUnit.MINUTES.between(instant, now).coerceAtLeast(0)
        return when {
            minutes < 1 -> "hace un momento"
            minutes < 60 -> "hace $minutes min"
            minutes < 60 * 24 -> "hace ${minutes / 60} h"
            else -> "hace ${minutes / (60 * 24)} d"
        }
    }
}
