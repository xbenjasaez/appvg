package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset

data class VirgiPathWalk(
    val strokeId: Long,
    val resampledPath: List<Offset>,
    val progress: Float,
    val position: Offset,
    val facingRight: Boolean
)
