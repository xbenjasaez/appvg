package cl.ipvg.docentecalma.ui.screens.recommendations

import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.Recommendation

data class RecommendationsUiState(
    val isLoading: Boolean = true,
    val checkIn: EmotionalCheckIn? = null,
    val recommendation: Recommendation? = null,
    val historyId: Long? = null,
    val acknowledged: Boolean = false,
    val error: String? = null
) {
    val hasData: Boolean get() = recommendation != null && checkIn != null
    val showEmpty: Boolean get() = !isLoading && error == null && !hasData
}

sealed interface RecommendationsEvent {
    data object OnAcknowledge : RecommendationsEvent
    data object OnRetry : RecommendationsEvent
    data object DismissError : RecommendationsEvent
}
