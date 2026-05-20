package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

import cl.ipvg.docentecalma.ui.mascot.MascotState

internal object BreathingSessionConfig {
    const val TOTAL_CYCLES = 3

    val phases: List<BreathingPhase> = listOf(
        BreathingPhase.Inhale,
        BreathingPhase.Hold,
        BreathingPhase.Exhale,
        BreathingPhase.Rest
    )

    val TOTAL_PHASE_STEPS: Int = TOTAL_CYCLES * phases.size

    fun mascotFor(phase: BreathingPhase): MascotState = when (phase) {
        BreathingPhase.Inhale -> MascotState.Listening
        BreathingPhase.Hold,
        BreathingPhase.Exhale -> MascotState.EmotionCalm
        BreathingPhase.Rest -> MascotState.Idle
    }

    fun progressStepIndex(cycle: Int, phase: BreathingPhase): Int {
        val phaseIndex = phases.indexOf(phase).coerceAtLeast(0)
        return (cycle - 1) * phases.size + phaseIndex
    }
}
