package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

private const val MIN_ORB_SCALE = 0.55f
private const val MAX_ORB_SCALE = 1.0f

enum class BreathingPhase(
    val durationSeconds: Int,
    val label: String,
    val startScale: Float,
    val endScale: Float
) {
    Inhale(
        durationSeconds = 4,
        label = BreathingCopy.phaseInhale,
        startScale = MIN_ORB_SCALE,
        endScale = MAX_ORB_SCALE
    ),
    Hold(
        durationSeconds = 2,
        label = BreathingCopy.phaseHold,
        startScale = MAX_ORB_SCALE,
        endScale = MAX_ORB_SCALE
    ),
    Exhale(
        durationSeconds = 6,
        label = BreathingCopy.phaseExhale,
        startScale = MAX_ORB_SCALE,
        endScale = MIN_ORB_SCALE
    ),
    Rest(
        durationSeconds = 2,
        label = BreathingCopy.phaseRest,
        startScale = MIN_ORB_SCALE,
        endScale = MIN_ORB_SCALE
    );

    val animationMillis: Int get() = durationSeconds * 1_000
}
