package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.geometry.Offset

data class TranquilLightUiState(
    val screenPhase: TranquilLightScreenPhase = TranquilLightScreenPhase.Intro,
    val lights: List<FloatingLight> = emptyList(),
    val particles: List<LightParticle> = emptyList(),
    val collectedCount: Int = 0,
    val lanternGlow: Float = 0f,
    val sceneVitality: Float = 0f,
    val lanternPulse: Float = 0f,
    val clockMs: Long = System.currentTimeMillis(),
    val playfieldSize: Offset = Offset.Zero,
    val isTravelInProgress: Boolean = false
) {
    val totalLights: Int = TranquilLightSessionConfig.TOTAL_LIGHTS

    val progressLabel: String
        get() = TranquilLightCopy.lightProgress(collectedCount, totalLights)

    val isInputEnabled: Boolean
        get() = screenPhase == TranquilLightScreenPhase.Playing &&
            !isTravelInProgress &&
            collectedCount < totalLights

    val isDragging: Boolean
        get() = lights.any { it.state == LightMotionState.Dragging }
}

sealed interface TranquilLightEvent {
    data object OnStart : TranquilLightEvent
    data object OnExit : TranquilLightEvent
    data object OnRepeat : TranquilLightEvent
    data object OnFinishGoBack : TranquilLightEvent
    data class OnDragStart(val offsetPx: Offset) : TranquilLightEvent
    data class OnDrag(val offsetPx: Offset) : TranquilLightEvent
    data object OnDragEnd : TranquilLightEvent
    data class OnPlayfieldSizeChanged(val sizePx: Offset) : TranquilLightEvent
}
