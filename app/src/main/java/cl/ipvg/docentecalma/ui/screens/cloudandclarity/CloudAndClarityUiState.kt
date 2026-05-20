package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.ui.geometry.Offset

data class CloudAndClarityUiState(
    val screenPhase: CloudAndClarityScreenPhase = CloudAndClarityScreenPhase.Intro,
    val sceneVariant: CloudSceneVariant = CloudSceneVariant.MorningBreeze,
    val maskCells: FloatArray = fullMaskSnapshot(),
    val clearedPercent: Int = 0,
    val sceneVitality: Float = 0f,
    val playfieldSize: Offset = Offset.Zero,
    val clockMs: Long = System.currentTimeMillis(),
    val lastErasePx: Offset? = null,
    val erasePulse: Float = 0f,
    val isErasing: Boolean = false,
    val mistSeed: Int = 42
) {
    val progressLabel: String
        get() = CloudAndClarityCopy.clearedProgress(clearedPercent)

    val isInputEnabled: Boolean
        get() = screenPhase == CloudAndClarityScreenPhase.Playing &&
            clearedPercent < completionPercentTarget

    private val completionPercentTarget: Int = 98

    companion object {
        fun fullMaskSnapshot(): FloatArray =
            FloatArray(CloudAndClaritySessionConfig.MASK_COLS * CloudAndClaritySessionConfig.MASK_ROWS) { 1f }
    }
}

sealed interface CloudAndClarityEvent {
    data object OnStart : CloudAndClarityEvent
    data object OnExit : CloudAndClarityEvent
    data object OnRepeat : CloudAndClarityEvent
    data object OnFinishGoBack : CloudAndClarityEvent
    data class OnEraseStart(val offsetPx: Offset) : CloudAndClarityEvent
    data class OnErase(val offsetPx: Offset) : CloudAndClarityEvent
    data object OnEraseEnd : CloudAndClarityEvent
    data class OnPlayfieldSizeChanged(val sizePx: Offset) : CloudAndClarityEvent
}
