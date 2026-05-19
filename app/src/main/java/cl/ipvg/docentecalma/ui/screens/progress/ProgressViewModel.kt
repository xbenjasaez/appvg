package cl.ipvg.docentecalma.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import cl.ipvg.docentecalma.data.repository.RecommendationHistoryRepository
import cl.ipvg.docentecalma.data.repository.SelfAssessmentRepository
import cl.ipvg.docentecalma.domain.mapper.EmotionLabels
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.RecommendationType
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import cl.ipvg.docentecalma.domain.rules.ProgressCalculator
import cl.ipvg.docentecalma.util.DateTimeFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel de progreso personal.
 *
 * Combina chequeos, autoevaluaciones y pasos guardados; los agregados viven en
 * [ProgressCalculator].
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    emotionalRepository: EmotionalRepository,
    selfAssessmentRepository: SelfAssessmentRepository,
    recommendationHistoryRepository: RecommendationHistoryRepository
) : ViewModel() {

    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProgressUiState> = combine(
        emotionalRepository.observeAll().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        selfAssessmentRepository.observeAll().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        recommendationHistoryRepository.observeAll().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        errorState
    ) { checkIns, assessments, recommendations, err ->
        buildState(
            checkIns = checkIns,
            assessments = assessments,
            recommendations = recommendations,
            now = Instant.now(),
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ProgressUiState(isLoading = true)
    )

    fun onEvent(event: ProgressEvent) {
        when (event) {
            ProgressEvent.DismissError -> errorState.update { null }
        }
    }

    private fun buildState(
        checkIns: List<EmotionalCheckIn>,
        assessments: List<SelfAssessment>,
        recommendations: List<RecommendationHistory>,
        now: Instant,
        error: String?
    ): ProgressUiState {
        val zone = ZoneId.systemDefault()
        val hasAny =
            checkIns.isNotEmpty() || assessments.isNotEmpty() || recommendations.isNotEmpty()

        if (!hasAny) {
            return ProgressUiState(
                isLoading = false,
                error = error,
                hasAnySavedActivity = false
            )
        }

        val activityInstants = buildList {
            checkIns.forEach { add(it.createdAt) }
            assessments.forEach { add(it.createdAt) }
            recommendations.forEach { add(it.createdAt) }
        }

        val activeDays = ProgressCalculator.distinctActiveLocalDays(
            instants = activityInstants,
            zone = zone,
            now = now,
            lastDays = CONTINUITY_DAYS
        )

        val lastInstant = activityInstants.maxOrNull()
        val lastRelative = lastInstant?.let { DateTimeFormatters.relative(it, now) }

        val last7 = ProgressCalculator.aggregate(
            ProgressCalculator.windowOf(checkIns, lastDays = 7, now = now),
            windowDays = 7
        )
        val last30List = ProgressCalculator.windowOf(checkIns, lastDays = 30, now = now)

        val assessmentsSorted = assessments.sortedByDescending { it.createdAt }
        val exerciseRecCount = recommendations.count { it.type == RecommendationType.EXERCISE }

        val insights = ProgressCalculator.personalInsightLines(
            activeDaysLast14 = activeDays,
            last7 = last7,
            checkIns = checkIns,
            assessmentsSortedNewestFirst = assessmentsSorted,
            exerciseRecommendationCount = exerciseRecCount,
            now = now,
            zone = zone
        )

        val timeline = ProgressCalculator.mergePersonalTimeline(
            checkIns = checkIns,
            assessments = assessments,
            recommendations = recommendations,
            maxItems = TIMELINE_MAX
        ).map { entry -> mapTimelineRow(entry, now) }

        val selfRows = assessmentsSorted.take(SELF_PREVIEW).map { mapSelfRow(it, now) }

        val emotionalSummary = if (checkIns.isEmpty()) {
            null
        } else {
            EmotionalRecentSummary(
                checkInsLast7 = last7.totalCheckIns,
                averageIntensity = last7.averageIntensity,
                mostFrequentLabel = last7.mostFrequentEmotion?.let(EmotionLabels::displayName),
                checkInsLast30 = last30List.size,
                totalCheckIns = checkIns.size
            )
        }

        return ProgressUiState(
            isLoading = false,
            error = error,
            hasAnySavedActivity = true,
            continuityWindowDays = CONTINUITY_DAYS,
            activeDaysInContinuityWindow = activeDays,
            lastActivityRelative = lastRelative,
            insightLines = insights,
            timeline = timeline,
            selfAssessmentRows = selfRows,
            emotionalSummary = emotionalSummary,
            savedStepsCount = recommendations.size,
            exerciseStepsCount = exerciseRecCount
        )
    }

    private fun mapSelfRow(assessment: SelfAssessment, now: Instant): ProgressSelfAssessmentRow {
        val title = when (assessment.evaluationType) {
            SelfAssessmentEvaluationType.INITIAL -> "Autoevaluación · primera vez"
            SelfAssessmentEvaluationType.PERIODIC -> "Autoevaluación · seguimiento"
        }
        val scoreLine =
            "Sumatoria ${assessment.totalScore}/${SelfAssessment.MAX_TOTAL} " +
                "(autoinforme sobre la semana, no es un diagnóstico)"
        return ProgressSelfAssessmentRow(
            id = assessment.id,
            title = title,
            scoreLine = scoreLine,
            relative = DateTimeFormatters.relative(assessment.createdAt, now)
        )
    }

    private fun mapTimelineRow(
        entry: ProgressCalculator.PersonalTimelineEntry,
        now: Instant
    ): ProgressTimelineRow = when (entry) {
        is ProgressCalculator.PersonalTimelineEntry.CheckInEntry -> {
            val c = entry.checkIn
            ProgressTimelineRow(
                primary = "Chequeo · ${EmotionLabels.displayName(c.emotion)}",
                secondary = buildString {
                    append("Intensidad ${c.intensity}/5")
                    if (!c.note.isNullOrBlank()) {
                        append(" · ")
                        append(ellipsize(c.note, NOTE_MAX))
                    }
                },
                relative = DateTimeFormatters.relative(c.createdAt, now)
            )
        }

        is ProgressCalculator.PersonalTimelineEntry.AssessmentEntry -> {
            val a = entry.assessment
            val kind = when (a.evaluationType) {
                SelfAssessmentEvaluationType.INITIAL -> "primera vez"
                SelfAssessmentEvaluationType.PERIODIC -> "seguimiento"
            }
            ProgressTimelineRow(
                primary = "Autoevaluación ($kind)",
                secondary = "Sumatoria ${a.totalScore}/${SelfAssessment.MAX_TOTAL}",
                relative = DateTimeFormatters.relative(a.createdAt, now)
            )
        }

        is ProgressCalculator.PersonalTimelineEntry.SavedRecommendationEntry -> {
            val r = entry.history
            ProgressTimelineRow(
                primary = recommendationTitle(r),
                secondary = ellipsize(r.summary, SUMMARY_MAX),
                relative = DateTimeFormatters.relative(r.createdAt, now)
            )
        }
    }

    private fun recommendationTitle(history: RecommendationHistory): String {
        val base = when (history.type) {
            RecommendationType.IMMEDIATE -> "Paso guardado · apoyo inmediato"
            RecommendationType.EXERCISE -> "Paso guardado · ejercicio o pausa"
            RecommendationType.CHAT -> "Paso guardado · conversación"
            RecommendationType.PROFESSIONAL -> "Paso guardado · orientación profesional"
        }
        return "$base · ${EmotionLabels.displayName(history.emotion)}"
    }

    private fun ellipsize(text: String, max: Int): String {
        val t = text.trim()
        if (t.length <= max) return t
        return t.take(max - 1).trimEnd() + "…"
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val CONTINUITY_DAYS = 14
        const val TIMELINE_MAX = 20
        const val SELF_PREVIEW = 5
        const val SUMMARY_MAX = 96
        const val NOTE_MAX = 72
    }
}
