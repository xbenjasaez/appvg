package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset

data class TraceAndReleaseUiState(
    val screenPhase: TraceScreenPhase = TraceScreenPhase.Intro,
    val strokes: List<TraceStroke> = emptyList(),
    val activeStroke: TraceStroke? = null,
    val clockMs: Long = System.currentTimeMillis(),
    val clearAcknowledged: Boolean = false,
    val guidedStrokesCompleted: Int = 0,
    val touchParticles: List<TraceParticle> = emptyList(),
    val pathSprouts: List<PathSprout> = emptyList(),
    val interactionMode: TraceInteractionMode = TraceInteractionMode.Drawing,
    val virgiWalk: VirgiPathWalk? = null
) {
    val guidedProgressLabel: String?
        get() = if (guidedStrokesCompleted < TraceSessionConfig.GUIDED_STROKES) {
            TraceAndReleaseCopy.strokeProgress(
                guidedStrokesCompleted + 1,
                TraceSessionConfig.GUIDED_STROKES
            )
        } else {
            null
        }

    val isInputEnabled: Boolean
        get() = interactionMode == TraceInteractionMode.Drawing &&
            screenPhase == TraceScreenPhase.Playing
}

sealed interface TraceAndReleaseEvent {
    data object OnStart : TraceAndReleaseEvent
    data object OnClear : TraceAndReleaseEvent
    data object OnExit : TraceAndReleaseEvent
    data object OnRequestClose : TraceAndReleaseEvent
    data object OnContinuePlaying : TraceAndReleaseEvent
    data object OnFinishGoBack : TraceAndReleaseEvent
    data class OnDragStart(val offset: Offset) : TraceAndReleaseEvent
    data class OnDrag(val offset: Offset) : TraceAndReleaseEvent
    data object OnDragEnd : TraceAndReleaseEvent
}
