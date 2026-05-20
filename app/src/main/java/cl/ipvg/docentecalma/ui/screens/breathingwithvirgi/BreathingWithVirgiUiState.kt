package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

data class BreathingWithVirgiUiState(
    val screenPhase: BreathingScreenPhase = BreathingScreenPhase.Intro,
    val currentCycle: Int = 1,
    val totalCycles: Int = BreathingSessionConfig.TOTAL_CYCLES,
    val phase: BreathingPhase = BreathingPhase.Inhale,
    val phaseSecondsRemaining: Int = BreathingPhase.Inhale.durationSeconds,
    val orbTargetScale: Float = BreathingPhase.Inhale.endScale,
    val orbAnimationMillis: Int = BreathingPhase.Inhale.animationMillis,
    val isSessionActive: Boolean = false
) {
    val phaseLabel: String get() = phase.label

    val cycleProgressLabel: String
        get() = BreathingCopy.cycleProgress(currentCycle, totalCycles)

    val sessionProgress: Float
        get() {
            val step = BreathingSessionConfig.progressStepIndex(currentCycle, phase)
            return (step + 1).toFloat() / BreathingSessionConfig.TOTAL_PHASE_STEPS
        }
}

sealed interface BreathingWithVirgiEvent {
    data object OnStart : BreathingWithVirgiEvent
    data object OnExitSession : BreathingWithVirgiEvent
    data object OnRepeat : BreathingWithVirgiEvent
    data object OnFinishGoBack : BreathingWithVirgiEvent
}
