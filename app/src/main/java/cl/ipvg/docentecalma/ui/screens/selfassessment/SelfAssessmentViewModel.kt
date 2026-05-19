package cl.ipvg.docentecalma.ui.screens.selfassessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.analytics.PilotFlowDurationBuckets
import cl.ipvg.docentecalma.data.analytics.PilotFlowSecondaryKeys
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.data.repository.SelfAssessmentRepository
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import cl.ipvg.docentecalma.domain.model.SelfAssessmentSuggestion
import cl.ipvg.docentecalma.domain.rules.SelfAssessmentFeedbackRules
import cl.ipvg.docentecalma.util.DateTimeFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

enum class SelfAssessmentPhase {
    INTRO,
    QUESTIONS,
    RESULT
}

data class SelfAssessmentHistoryRowUi(
    val id: Long,
    val dateLabel: String,
    val totalScore: Int,
    val typeLabel: String
)

data class SelfAssessmentResultUi(
    val totalScore: Int,
    val maxTotal: Int,
    val typeLabel: String,
    val recordedAtLabel: String,
    val summary: String,
    val comparison: String?,
    val suggestions: List<SelfAssessmentSuggestion>
)

data class SelfAssessmentUiState(
    val phase: SelfAssessmentPhase = SelfAssessmentPhase.INTRO,
    val questionIndex: Int = 0,
    val answers: List<Int?> = List(SelfAssessment.QUESTION_COUNT) { null },
    val historyPreview: List<SelfAssessmentHistoryRowUi> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val result: SelfAssessmentResultUi? = null
)

sealed interface SelfAssessmentEvent {
    data object Start : SelfAssessmentEvent
    data class OnAnswerSelected(val value: Int) : SelfAssessmentEvent
    data object OnNext : SelfAssessmentEvent
    data object OnQuestionBack : SelfAssessmentEvent
    data object OnNewAssessment : SelfAssessmentEvent
    data object DismissError : SelfAssessmentEvent
}

@HiltViewModel
class SelfAssessmentViewModel @Inject constructor(
    private val repository: SelfAssessmentRepository,
    private val feedbackRules: SelfAssessmentFeedbackRules,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelfAssessmentUiState())
    val uiState: StateFlow<SelfAssessmentUiState> = _uiState.asStateFlow()

    private val flowStartedAtMs: Long = System.currentTimeMillis()

    init {
        repository.observeAll()
            .onEach { list -> updateHistoryPreview(list) }
            .catch {
                _uiState.update { s ->
                    s.copy(error = it.message ?: "Error cargando historial de autoevaluaciones.")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SelfAssessmentEvent) {
        when (event) {
            SelfAssessmentEvent.Start -> {
                _uiState.update {
                    it.copy(
                        phase = SelfAssessmentPhase.QUESTIONS,
                        questionIndex = 0,
                        answers = List(SelfAssessment.QUESTION_COUNT) { null },
                        error = null,
                        result = null
                    )
                }
                viewModelScope.launch {
                    pilotAnalyticsRepository.record(PilotEventNames.SELF_ASSESSMENT_STARTED)
                }
            }
            is SelfAssessmentEvent.OnAnswerSelected -> {
                val idx = _uiState.value.questionIndex
                if (idx !in 0 until SelfAssessment.QUESTION_COUNT) return
                val v = event.value.coerceIn(SelfAssessment.SCORE_RANGE)
                _uiState.update { s ->
                    val next = s.answers.toMutableList().also { it[idx] = v }
                    s.copy(answers = next, error = null)
                }
            }
            SelfAssessmentEvent.OnNext -> onNext()
            SelfAssessmentEvent.OnQuestionBack -> _uiState.update { s ->
                when {
                    s.phase != SelfAssessmentPhase.QUESTIONS -> s
                    s.questionIndex <= 0 ->
                        s.copy(phase = SelfAssessmentPhase.INTRO, questionIndex = 0, error = null)
                    else -> s.copy(questionIndex = s.questionIndex - 1, error = null)
                }
            }
            SelfAssessmentEvent.OnNewAssessment -> _uiState.update {
                SelfAssessmentUiState(historyPreview = it.historyPreview)
            }
            SelfAssessmentEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun onNext() {
        val s = _uiState.value
        if (s.phase != SelfAssessmentPhase.QUESTIONS || s.isSaving) return
        val current = s.answers.getOrNull(s.questionIndex)
        if (current == null) {
            _uiState.update {
                it.copy(error = "Elige una opción del 1 al 5 para continuar.")
            }
            return
        }
        if (s.questionIndex < SelfAssessment.QUESTION_COUNT - 1) {
            _uiState.update { it.copy(questionIndex = it.questionIndex + 1, error = null) }
            return
        }
        submit(s.answers.map { it!! })
    }

    private fun submit(answers: List<Int>) {
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val previous = repository.getLatest()
                val id = repository.saveAnswers(answers)
                val saved = repository.getById(id)
                    ?: error("No se pudo leer la autoevaluación guardada.")
                val typeLabel = typeDisplay(saved.evaluationType)
                val resultUi = SelfAssessmentResultUi(
                    totalScore = saved.totalScore,
                    maxTotal = SelfAssessment.MAX_TOTAL,
                    typeLabel = typeLabel,
                    recordedAtLabel = DateTimeFormatters.full(saved.createdAt),
                    summary = feedbackRules.summaryForScore(saved.totalScore),
                    comparison = feedbackRules.comparisonLine(saved.totalScore, previous),
                    suggestions = feedbackRules.suggestionsForScore(saved.totalScore)
                )
                _uiState.update {
                    it.copy(
                        phase = SelfAssessmentPhase.RESULT,
                        isSaving = false,
                        result = resultUi,
                        error = null
                    )
                }
                pilotAnalyticsRepository.record(PilotEventNames.SELF_ASSESSMENT_COMPLETED)
            }.onFailure { t ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = t.message ?: "No se pudo guardar la autoevaluación."
                    )
                }
            }
        }
    }

    private fun updateHistoryPreview(assessments: List<SelfAssessment>) {
        val preview = assessments.take(8).map { a ->
            SelfAssessmentHistoryRowUi(
                id = a.id,
                dateLabel = DateTimeFormatters.short(a.createdAt),
                totalScore = a.totalScore,
                typeLabel = typeDisplay(a.evaluationType)
            )
        }
        _uiState.update { it.copy(historyPreview = preview) }
    }

    private fun typeDisplay(type: SelfAssessmentEvaluationType): String = when (type) {
        SelfAssessmentEvaluationType.INITIAL -> "Primera autoevaluación"
        SelfAssessmentEvaluationType.PERIODIC -> "Seguimiento"
    }

    override fun onCleared() {
        super.onCleared()
        val seconds = ((System.currentTimeMillis() - flowStartedAtMs) / 1000).toInt().coerceAtLeast(0)
        val bucket = PilotFlowDurationBuckets.bucket(seconds)
        runBlocking {
            pilotAnalyticsRepository.record(
                PilotEventNames.FLOW_DURATION_BUCKET,
                secondaryKey = PilotFlowSecondaryKeys.SELF_ASSESSMENT,
                intMeta = bucket
            )
        }
    }
}
