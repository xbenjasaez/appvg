package cl.ipvg.docentecalma.ui.screens.classroomguidance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.repository.ClassroomGuidanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassroomGuidanceViewModel @Inject constructor(
    private val repository: ClassroomGuidanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassroomGuidanceUiState())
    val uiState: StateFlow<ClassroomGuidanceUiState> = _uiState.asStateFlow()

    init {
        observeScenarios()
    }

    fun onEvent(event: ClassroomGuidanceEvent) {
        when (event) {
            is ClassroomGuidanceEvent.OnScenarioSelected -> {
                val scenario = repository.getScenario(event.scenarioId)
                _uiState.update { it.copy(selectedScenario = scenario) }
            }
            ClassroomGuidanceEvent.OnCloseDetail -> _uiState.update {
                it.copy(selectedScenario = null)
            }
            ClassroomGuidanceEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun observeScenarios() {
        viewModelScope.launch {
            repository.observeScenarios()
                .catch { t ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = t.message ?: "Error cargando escenarios."
                        )
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            scenarios = list,
                            error = null
                        )
                    }
                }
        }
    }
}
