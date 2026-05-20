package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset

data class TracePoint(
    val offset: Offset,
    val timestampMs: Long
)

data class TraceStroke(
    val id: Long,
    val points: List<TracePoint>,
    val resampledPath: List<Offset> = emptyList(),
    val walkCompleted: Boolean = false
)

data class TraceParticle(
    val id: Long,
    val offset: Offset,
    val bornMs: Long,
    val tintIndex: Int
)

enum class TraceInteractionMode {
    Drawing,
    VirgiWalking
}
