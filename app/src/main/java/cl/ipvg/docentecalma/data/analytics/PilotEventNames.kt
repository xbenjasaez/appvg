package cl.ipvg.docentecalma.data.analytics

/**
 * Nombres estables de eventos del piloto (MVP). Solo cadenas fijas; sin PII.
 */
object PilotEventNames {
    const val ONBOARDING_COMPLETED: String = "onboarding_completed"
    const val CONSENT_ACCEPTED: String = "consent_accepted"
    /** Una fila por día local en que la app se abrió (clave en [secondaryKey], sin PII). */
    const val DAY_ACTIVE: String = "day_active"
    const val SELF_ASSESSMENT_STARTED: String = "self_assessment_started"
    const val SELF_ASSESSMENT_COMPLETED: String = "self_assessment_completed"
    const val MODULE_STARTED: String = "module_started"
    const val MODULE_COMPLETED: String = "module_completed"
    const val FEEDBACK_SUBMITTED: String = "feedback_submitted"
    const val CHAT_OPENED: String = "chat_opened"
    const val CHAT_BLOCKED_BY_SAFETY: String = "chat_blocked_by_safety"
    const val CHAT_MESSAGE_SENT: String = "chat_message_sent"
    /**
     * Tiempo aproximado en pantalla al salir del flujo.
     * [intMeta] = [PilotFlowDurationBuckets]; [secondaryKey] = id de flujo (ver [PilotFlowSecondaryKeys]).
     */
    const val FLOW_DURATION_BUCKET: String = "flow_duration_bucket"
}

/**
 * Valores de [PilotAnalyticsEventEntity.secondaryKey] para duración por flujo (sin texto libre).
 */
object PilotFlowSecondaryKeys {
    const val SELF_ASSESSMENT: String = "flow_self_assessment"
    const val SUPPORT_CHAT: String = "flow_support_chat"
    fun micromodule(moduleId: String): String = "flow_micromodule_$moduleId"
}
