package cl.ipvg.docentecalma.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.repository.ChatRepository
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import cl.ipvg.docentecalma.data.repository.RecommendationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de Historial.
 *
 * Combina tres flujos reactivos independientes (chequeos, recomendaciones,
 * sesiones de chat) en un único [HistoryUiState]. Cada flujo está blindado
 * con `catch` para que el fallo de una fuente no deje al resto en blanco.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val emotionalRepository: EmotionalRepository,
    recommendationHistoryRepository: RecommendationHistoryRepository,
    chatRepository: ChatRepository
) : ViewModel() {

    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        emotionalRepository.observeAll().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        recommendationHistoryRepository.observeAll().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        chatRepository.observeSessionSummaries().catch { t ->
            emit(emptyList())
            errorState.value = t.message
        },
        errorState
    ) { checkIns, recommendations, chatSessions, err ->
        HistoryUiState(
            isLoading = false,
            checkIns = checkIns,
            recommendations = recommendations,
            chatSessions = chatSessions,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OnDeleteCheckIn -> deleteCheckIn(event.id)
            HistoryEvent.DismissError -> errorState.update { null }
        }
    }

    private fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            runCatching { emotionalRepository.delete(id) }
                .onFailure { t ->
                    errorState.value = t.message ?: "No se pudo eliminar el chequeo."
                }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
