package cl.ipvg.docentecalma.ui.screens.history

import cl.ipvg.docentecalma.domain.model.ChatSessionSummary
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.RecommendationHistory

/**
 * Estado UI de la pantalla de Historial.
 *
 * Agrupa tres fuentes de datos distintas ya proyectadas a dominio:
 * - Chequeos emocionales completos.
 * - Recomendaciones registradas (ligadas opcionalmente a un chequeo).
 * - Resúmenes de sesiones de chat (sin los mensajes detallados).
 */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val checkIns: List<EmotionalCheckIn> = emptyList(),
    val recommendations: List<RecommendationHistory> = emptyList(),
    val chatSessions: List<ChatSessionSummary> = emptyList(),
    val error: String? = null
) {
    val showEmpty: Boolean
        get() = !isLoading &&
            checkIns.isEmpty() &&
            recommendations.isEmpty() &&
            chatSessions.isEmpty() &&
            error == null
}

sealed interface HistoryEvent {
    data class OnDeleteCheckIn(val id: Long) : HistoryEvent
    data object DismissError : HistoryEvent
}
