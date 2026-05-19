package cl.ipvg.docentecalma.ui.screens.emotionalcheckin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmotionalCheckInViewModel @Inject constructor(
    private val emotionalRepository: EmotionalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmotionalCheckInUiState())
    val uiState: StateFlow<EmotionalCheckInUiState> = _uiState.asStateFlow()

    private val _effects = Channel<EmotionalCheckInEffect>(Channel.BUFFERED)
    val effects: Flow<EmotionalCheckInEffect> = _effects.receiveAsFlow()

    fun onEvent(event: EmotionalCheckInEvent) {
        when (event) {
            is EmotionalCheckInEvent.OnEmotionSelected -> _uiState.update {
                it.copy(selectedEmotion = event.emotion, error = null)
            }
            is EmotionalCheckInEvent.OnIntensityChanged -> _uiState.update {
                it.copy(
                    intensity = event.intensity.coerceIn(it.intensityRange),
                    error = null
                )
            }
            is EmotionalCheckInEvent.OnNoteChanged -> _uiState.update {
                it.copy(
                    note = event.note.take(EmotionalCheckInUiState.NOTE_MAX_LENGTH),
                    error = null
                )
            }
            EmotionalCheckInEvent.OnSave -> save()
            EmotionalCheckInEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun save() {
        val current = _uiState.value
        val emotion = current.selectedEmotion ?: run {
            _uiState.update { it.copy(error = "Selecciona una emoción para continuar.") }
            return
        }
        if (current.isSaving) return

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                emotionalRepository.save(
                    emotion = emotion,
                    intensity = current.intensity,
                    note = current.note
                )
            }.onSuccess { id ->
                _uiState.update { EmotionalCheckInUiState() }
                _effects.trySend(EmotionalCheckInEffect.Saved(id))
            }.onFailure { t ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = t.message ?: "No se pudo guardar el chequeo."
                    )
                }
            }
        }
    }
}
