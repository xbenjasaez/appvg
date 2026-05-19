package cl.ipvg.docentecalma.ui.screens.pilotmetrics

import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import java.util.Locale

/**
 * Etiquetas y orden de presentación para eventos del piloto (solo capa UI).
 */
object PilotMetricLabels {

    private val DISPLAY_ORDER = listOf(
        PilotEventNames.DAY_ACTIVE,
        PilotEventNames.ONBOARDING_COMPLETED,
        PilotEventNames.CONSENT_ACCEPTED,
        PilotEventNames.MODULE_STARTED,
        PilotEventNames.MODULE_COMPLETED,
        PilotEventNames.SELF_ASSESSMENT_STARTED,
        PilotEventNames.SELF_ASSESSMENT_COMPLETED,
        PilotEventNames.CHAT_OPENED,
        PilotEventNames.CHAT_MESSAGE_SENT,
        PilotEventNames.CHAT_BLOCKED_BY_SAFETY,
        PilotEventNames.FEEDBACK_SUBMITTED,
        PilotEventNames.FLOW_DURATION_BUCKET
    )

    private val localeEsCl = Locale("es", "CL")

    fun sortIndex(eventType: String): Int {
        val i = DISPLAY_ORDER.indexOf(eventType)
        return if (i >= 0) i else DISPLAY_ORDER.size
    }

    fun humanLabel(eventType: String): String = when (eventType) {
        PilotEventNames.CHAT_MESSAGE_SENT -> "Mensajes enviados en chat"
        PilotEventNames.CHAT_OPENED -> "Aperturas del chat"
        PilotEventNames.CHAT_BLOCKED_BY_SAFETY -> "Respuestas de chat detenidas por seguridad"
        PilotEventNames.CONSENT_ACCEPTED -> "Consentimientos aceptados"
        PilotEventNames.DAY_ACTIVE -> "Días activos"
        PilotEventNames.FEEDBACK_SUBMITTED -> "Retroalimentaciones enviadas"
        PilotEventNames.FLOW_DURATION_BUCKET -> "Duración aproximada de uso"
        PilotEventNames.MODULE_COMPLETED -> "Módulos completados"
        PilotEventNames.MODULE_STARTED -> "Módulos iniciados"
        PilotEventNames.ONBOARDING_COMPLETED -> "Onboarding completado"
        PilotEventNames.SELF_ASSESSMENT_COMPLETED -> "Autoevaluaciones completadas"
        PilotEventNames.SELF_ASSESSMENT_STARTED -> "Autoevaluaciones iniciadas"
        else -> eventType
            .replace('_', ' ')
            .split(' ')
            .joinToString(" ") { w ->
                w.replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase(localeEsCl) else c.toString()
                }
            }
    }
}
