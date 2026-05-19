package cl.ipvg.docentecalma.ui.screens.recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.data.repository.RecommendationHistoryRepository
import cl.ipvg.docentecalma.domain.rules.RecommendationEngine
import cl.ipvg.docentecalma.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val emotionalRepository: EmotionalRepository,
    private val historyRepository: RecommendationHistoryRepository,
    private val engine: RecommendationEngine,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    private val checkInId: Long = savedStateHandle.get<Long>(
        Routes.ARG_CHECK_IN_ID
    ) ?: Routes.INVALID_CHECK_IN_ID

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: RecommendationsEvent) {
        when (event) {
            RecommendationsEvent.OnAcknowledge -> acknowledge()
            RecommendationsEvent.OnRetry -> load()
            RecommendationsEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun load() {
        if (checkInId <= 0L) {
            _uiState.value = RecommendationsUiState(
                isLoading = false,
                error = "Chequeo no válido."
            )
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val checkIn = emotionalRepository.getById(checkInId)
                    ?: error("No encontramos el chequeo #$checkInId.")
                val recommendation = engine.build(checkIn)
                val historySummary = buildString {
                    append(recommendation.title)
                    append(" — ")
                    append(recommendation.shortMessage)
                }
                val historyId = historyRepository.log(
                    checkInId = checkIn.id,
                    emotion = checkIn.emotion,
                    intensity = checkIn.intensity,
                    type = recommendation.toHistoryType(),
                    summary = historySummary,
                    acknowledged = false
                )
                Triple(checkIn, recommendation, historyId)
            }.onSuccess { (checkIn, recommendation, historyId) ->
                _uiState.value = RecommendationsUiState(
                    isLoading = false,
                    checkIn = checkIn,
                    recommendation = recommendation,
                    historyId = historyId,
                    acknowledged = false,
                    error = null
                )
            }.onFailure { t ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: "No pudimos generar la recomendación."
                    )
                }
            }
        }
    }

    private fun acknowledge() {
        val historyId = _uiState.value.historyId ?: return
        if (_uiState.value.acknowledged) return
        _uiState.update { it.copy(acknowledged = true) }
        viewModelScope.launch {
            runCatching { historyRepository.setAcknowledged(historyId, true) }
                .onSuccess {
                    pilotAnalyticsRepository.record(PilotEventNames.FEEDBACK_SUBMITTED)
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(
                            acknowledged = false,
                            error = t.message ?: "No se pudo marcar como aplicada."
                        )
                    }
                }
        }
    }
}
