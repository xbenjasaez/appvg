package cl.ipvg.docentecalma.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.preferences.PostUseFeedbackRepository
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del Home.
 *
 * Observa el último chequeo emocional desde Room y expone un [HomeUiState]
 * único. Usa un [MutableStateFlow] como fuente de verdad interna para que
 * los estados de error y éxito convivan sin flows auxiliares, y para que
 * la UI pueda descartar el error con [HomeEvent.DismissError] sin reiniciar el ViewModel.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    emotionalRepository: EmotionalRepository,
    private val postUseFeedbackRepository: PostUseFeedbackRepository,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var feedbackPromptJob: Job? = null

    init {
        emotionalRepository.observeLatest()
            .onEach { latest ->
                _uiState.update {
                    HomeUiState(
                        isLoading = false,
                        latestCheckIn = latest,
                        error = null,
                        feedbackSheetVisible = it.feedbackSheetVisible
                    )
                }
            }
            .catch { t ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: "Error cargando el último chequeo."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    /**
     * Invocar cuando la pantalla inicio entra en composición (cada vez que se vuelve desde otra ruta).
     */
    fun onHomeAppear() {
        feedbackPromptJob?.cancel()
        feedbackPromptJob = viewModelScope.launch {
            val eligible = runCatching { postUseFeedbackRepository.evaluateAfterHomeEntered() }
                .getOrDefault(false)
            if (!eligible) return@launch

            var waited = 0
            while (isActive && _uiState.value.isLoading && waited < 3_000) {
                delay(100)
                waited += 100
            }
            if (!isActive) return@launch
            if (_uiState.value.error != null) return@launch

            delay(1_200)
            if (!isActive) return@launch
            if (_uiState.value.error != null) return@launch

            _uiState.update { it.copy(feedbackSheetVisible = true) }
        }
    }

    fun onHomeHidden() {
        feedbackPromptJob?.cancel()
        feedbackPromptJob = null
    }

    fun onFeedbackDismiss() {
        _uiState.update { it.copy(feedbackSheetVisible = false) }
        viewModelScope.launch {
            runCatching { postUseFeedbackRepository.recordDismissal() }
        }
    }

    fun onFeedbackSubmit(satisfaction: Int, usefulness: Int, ease: Int, comment: String?) {
        _uiState.update { it.copy(feedbackSheetVisible = false) }
        viewModelScope.launch {
            runCatching {
                postUseFeedbackRepository.saveSubmission(satisfaction, usefulness, ease, comment)
                val avg = (satisfaction + usefulness + ease) / 3
                pilotAnalyticsRepository.record(
                    type = PilotEventNames.FEEDBACK_SUBMITTED,
                    intMeta = avg
                )
            }
        }
    }
}
