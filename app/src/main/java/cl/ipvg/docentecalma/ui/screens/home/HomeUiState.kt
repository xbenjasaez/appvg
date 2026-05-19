package cl.ipvg.docentecalma.ui.screens.home

import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val latestCheckIn: EmotionalCheckIn? = null,
    val error: String? = null,
    val feedbackSheetVisible: Boolean = false
) {
    val hasLatest: Boolean get() = latestCheckIn != null
    val showEmpty: Boolean get() = !isLoading && latestCheckIn == null && error == null
}

sealed interface HomeEvent {
    data object DismissError : HomeEvent
}
