package cl.ipvg.docentecalma.ui.screens.pilotmetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.local.dao.PilotEventCountRow
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackRepository
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackSnapshot
import cl.ipvg.docentecalma.data.repository.MicromoduleProgressRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.data.repository.PilotMetricsRollup
import cl.ipvg.docentecalma.data.repository.SelfAssessmentRepository
import cl.ipvg.docentecalma.domain.model.MicromoduleUserProgress
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog
import cl.ipvg.docentecalma.domain.rules.PilotEffectivenessCalculator
import cl.ipvg.docentecalma.util.DateTimeFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

data class PilotMetricRowUi(
    val eventType: String,
    val count: Int
)

data class PilotMetricsUiState(
    val effectiveness: PilotEffectivenessCalculator.PilotEffectivenessSnapshot,
    val rows: List<PilotMetricRowUi> = emptyList(),
    val totalEvents: Long = 0L,
    val distinctDaysWithEvents: Int = 0,
    val firstEventLabel: String? = null,
    val lastEventLabel: String? = null,
    val error: String? = null
)

private data class PilotAnalyticsSlice(
    val grouped: List<PilotEventCountRow>,
    val total: Long,
    val rollup: PilotMetricsRollup,
    val days14: Int
)

@HiltViewModel
class PilotMetricsViewModel @Inject constructor(
    pilotAnalyticsRepository: PilotAnalyticsRepository,
    selfAssessmentRepository: SelfAssessmentRepository,
    micromoduleProgressRepository: MicromoduleProgressRepository,
    postUseFeedbackRepository: PostUseFeedbackRepository
) : ViewModel() {

    private val errorState = MutableStateFlow<String?>(null)

    private val assessmentsFlow = selfAssessmentRepository.observeAll().catch { t ->
        emit(emptyList())
        errorState.update { t.message }
    }

    private val modulesFlow = micromoduleProgressRepository.observeAll().catch { t ->
        emit(emptyList<MicromoduleUserProgress>())
        errorState.update { t.message }
    }

    private val feedbackFlow = postUseFeedbackRepository.observeLastSubmission().catch { t ->
        emit(null)
        errorState.update { t.message }
    }

    private val pilotSliceFlow = combine(
        pilotAnalyticsRepository.observeGroupedCounts(),
        pilotAnalyticsRepository.observeTotalCount(),
        pilotAnalyticsRepository.observeRollup(),
        pilotAnalyticsRepository.observeDistinctLocalDayCountLast14Days()
    ) { grouped, total, rollup, days14 ->
        PilotAnalyticsSlice(
            grouped = grouped,
            total = total,
            rollup = rollup,
            days14 = days14
        )
    }

    val uiState: StateFlow<PilotMetricsUiState> = combine(
        pilotSliceFlow,
        assessmentsFlow,
        modulesFlow,
        feedbackFlow,
        errorState
    ) { slice: PilotAnalyticsSlice,
        assessments: List<SelfAssessment>,
        modules: List<MicromoduleUserProgress>,
        feedback: PostUseFeedbackSnapshot?,
        err: String? ->
        val snapshot = PilotEffectivenessCalculator.build(
            assessments = assessments,
            moduleProgress = modules,
            modulesTotal = MicromoduleCatalog.all.size,
            activeDistinctDaysLast14 = slice.days14,
            distinctPilotDaysAllTime = slice.rollup.distinctDaysWithEvents,
            totalPilotEvents = slice.total,
            feedbackSatisfaction = feedback?.satisfaction,
            feedbackUsefulness = feedback?.usefulness,
            feedbackEase = feedback?.ease
        )
        PilotMetricsUiState(
            effectiveness = snapshot,
            rows = slice.grouped.map { PilotMetricRowUi(eventType = it.eventType, count = it.count) },
            totalEvents = slice.total,
            distinctDaysWithEvents = slice.rollup.distinctDaysWithEvents,
            firstEventLabel = slice.rollup.firstEventEpochMs?.let {
                DateTimeFormatters.short(Instant.ofEpochMilli(it))
            },
            lastEventLabel = slice.rollup.lastEventEpochMs?.let {
                DateTimeFormatters.short(Instant.ofEpochMilli(it))
            },
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PilotMetricsUiState(
            effectiveness = PilotEffectivenessCalculator.build(
                assessments = emptyList(),
                moduleProgress = emptyList(),
                modulesTotal = MicromoduleCatalog.all.size,
                activeDistinctDaysLast14 = 0,
                distinctPilotDaysAllTime = 0,
                totalPilotEvents = 0L,
                feedbackSatisfaction = null,
                feedbackUsefulness = null,
                feedbackEase = null
            )
        )
    )

    fun dismissError() {
        errorState.update { null }
    }
}
