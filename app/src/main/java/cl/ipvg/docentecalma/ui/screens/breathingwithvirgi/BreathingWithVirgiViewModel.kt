package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreathingWithVirgiViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BreathingWithVirgiUiState())
    val uiState: StateFlow<BreathingWithVirgiUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null

    fun onEvent(event: BreathingWithVirgiEvent) {
        when (event) {
            BreathingWithVirgiEvent.OnStart -> startSession()
            BreathingWithVirgiEvent.OnExitSession -> cancelSession(resetToIntro = false)
            BreathingWithVirgiEvent.OnRepeat -> {
                cancelSession(resetToIntro = true)
            }
            BreathingWithVirgiEvent.OnFinishGoBack -> Unit
        }
    }

    private fun startSession() {
        cancelSession(resetToIntro = false)
        val firstPhase = BreathingPhase.Inhale
        _uiState.value = BreathingWithVirgiUiState(
            screenPhase = BreathingScreenPhase.Running,
            currentCycle = 1,
            phase = firstPhase,
            phaseSecondsRemaining = firstPhase.durationSeconds,
            orbTargetScale = firstPhase.endScale,
            orbAnimationMillis = firstPhase.animationMillis,
            isSessionActive = true
        )
        sessionJob = viewModelScope.launch {
            runSession()
        }
    }

    private suspend fun runSession() {
        for (cycle in 1..BreathingSessionConfig.TOTAL_CYCLES) {
            for (phase in BreathingSessionConfig.phases) {
                if (!applyPhase(cycle, phase)) return
                repeat(phase.durationSeconds) {
                    delay(1_000L)
                    _uiState.update { state ->
                        if (!state.isSessionActive) return@update state
                        state.copy(phaseSecondsRemaining = state.phaseSecondsRemaining - 1)
                    }
                }
            }
        }
        _uiState.update {
            it.copy(
                screenPhase = BreathingScreenPhase.Finished,
                isSessionActive = false
            )
        }
    }

    private fun applyPhase(cycle: Int, phase: BreathingPhase): Boolean {
        var shouldContinue = true
        _uiState.update { state ->
            if (!state.isSessionActive) {
                shouldContinue = false
                return@update state
            }
            state.copy(
                currentCycle = cycle,
                phase = phase,
                phaseSecondsRemaining = phase.durationSeconds,
                orbTargetScale = phase.endScale,
                orbAnimationMillis = phase.animationMillis
            )
        }
        return shouldContinue
    }

    private fun cancelSession(resetToIntro: Boolean) {
        sessionJob?.cancel()
        sessionJob = null
        if (resetToIntro) {
            _uiState.value = BreathingWithVirgiUiState()
        } else {
            _uiState.update { it.copy(isSessionActive = false) }
        }
    }

    override fun onCleared() {
        sessionJob?.cancel()
        super.onCleared()
    }
}
