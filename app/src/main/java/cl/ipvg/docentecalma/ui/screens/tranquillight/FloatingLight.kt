package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.geometry.Offset

enum class LightMotionState {
    Floating,
    Dragging,
    Traveling,
    Collected
}

data class FloatingLight(
    val id: Int,
    val baseOffset: Offset,
    val driftPhase: Float = 0f,
    val sizeScale: Float = 1f,
    val warmth: Float = 1f,
    val pulsePhase: Float = 0f,
    val driftMultiplier: Float = 1f,
    val state: LightMotionState = LightMotionState.Floating,
    val dragOffset: Offset? = null,
    val travelStartOffset: Offset? = null,
    val travelProgress: Float = 0f
)
