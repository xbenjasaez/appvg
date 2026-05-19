package cl.ipvg.docentecalma.ui.screens.emotionalcheckin

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn

data class EmotionalCheckInUiState(
    val emotions: List<Emotion> = Emotion.entries.toList(),
    val selectedEmotion: Emotion? = null,
    val intensity: Int = DEFAULT_INTENSITY,
    val note: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() = selectedEmotion != null && !isSaving &&
            intensity in EmotionalCheckIn.INTENSITY_RANGE

    val intensityRange: IntRange = EmotionalCheckIn.INTENSITY_RANGE

    companion object {
        const val DEFAULT_INTENSITY: Int = 3
        const val NOTE_MAX_LENGTH: Int = 280
    }
}

sealed interface EmotionalCheckInEvent {
    data class OnEmotionSelected(val emotion: Emotion) : EmotionalCheckInEvent
    data class OnIntensityChanged(val intensity: Int) : EmotionalCheckInEvent
    data class OnNoteChanged(val note: String) : EmotionalCheckInEvent
    data object OnSave : EmotionalCheckInEvent
    data object DismissError : EmotionalCheckInEvent
}

sealed interface EmotionalCheckInEffect {
    data class Saved(val checkInId: Long) : EmotionalCheckInEffect
}
