package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset

enum class SproutKind {
    Leaf,
    Bud,
    Bloom,
    Sparkle
}

data class PathSprout(
    val id: Long,
    val offset: Offset,
    val bornMs: Long,
    val kind: SproutKind,
    val rotationDeg: Float = 0f
)
