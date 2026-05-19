package cl.ipvg.docentecalma.ui.screens.classroomguidance

import cl.ipvg.docentecalma.domain.model.ClassroomScenario

data class ClassroomGuidanceUiState(
    val isLoading: Boolean = true,
    val scenarios: List<ClassroomScenario> = emptyList(),
    val selectedScenario: ClassroomScenario? = null,
    val error: String? = null
) {
    val showEmpty: Boolean get() = !isLoading && scenarios.isEmpty() && error == null
}

sealed interface ClassroomGuidanceEvent {
    data class OnScenarioSelected(val scenarioId: String) : ClassroomGuidanceEvent
    data object OnCloseDetail : ClassroomGuidanceEvent
    data object DismissError : ClassroomGuidanceEvent
}
