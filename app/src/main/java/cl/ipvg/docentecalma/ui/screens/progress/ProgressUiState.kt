package cl.ipvg.docentecalma.ui.screens.progress

/**
 * Estado UI de la pantalla de progreso personal.
 *
 * Los textos ya vienen listos para mostrar; la capa Compose no interpreta reglas de negocio.
 */
data class ProgressUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Hay al menos un chequeo, autoevaluación o paso guardado en la app. */
    val hasAnySavedActivity: Boolean = false,
    val continuityWindowDays: Int = 14,
    /** Días distintos con actividad en la ventana de continuidad. */
    val activeDaysInContinuityWindow: Int = 0,
    val lastActivityRelative: String? = null,
    val insightLines: List<String> = emptyList(),
    val timeline: List<ProgressTimelineRow> = emptyList(),
    val selfAssessmentRows: List<ProgressSelfAssessmentRow> = emptyList(),
    val emotionalSummary: EmotionalRecentSummary? = null,
    val savedStepsCount: Int = 0,
    val exerciseStepsCount: Int = 0
) {
    val showEmpty: Boolean
        get() = !isLoading && !hasAnySavedActivity && error == null
}

data class ProgressTimelineRow(
    val primary: String,
    val secondary: String,
    val relative: String
)

data class ProgressSelfAssessmentRow(
    val id: Long,
    val title: String,
    val scoreLine: String,
    val relative: String
)

/**
 * Resumen suave del registro emocional (solo si hay chequeos).
 */
data class EmotionalRecentSummary(
    val checkInsLast7: Int,
    val averageIntensity: Double?,
    val mostFrequentLabel: String?,
    val checkInsLast30: Int,
    val totalCheckIns: Int
)

sealed interface ProgressEvent {
    data object DismissError : ProgressEvent
}
