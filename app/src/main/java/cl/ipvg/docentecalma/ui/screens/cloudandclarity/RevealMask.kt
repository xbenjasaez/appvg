package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot
import kotlin.math.min

/**
 * Malla de opacidad de niebla (1 = cubierto, 0 = despejado).
 * Vive en el ViewModel; el Canvas solo la lee para pintar.
 */
internal class RevealMask(
    val cols: Int,
    val rows: Int,
    private val cells: FloatArray
) {
    val clearedFraction: Float
        get() {
            if (cells.isEmpty()) return 0f
            val epsilon = CloudAndClaritySessionConfig.CELL_CLEARED_EPSILON
            var clearedCells = 0
            for (value in cells) {
                if (value.coerceIn(0f, 1f) <= epsilon) {
                    clearedCells++
                }
            }
            return (clearedCells.toFloat() / cells.size).coerceIn(0f, 1f)
        }

    fun cellValue(col: Int, row: Int): Float = cells[index(col, row)]

    fun eraseNormalized(center: Offset, radiusNorm: Float) {
        if (cols <= 0 || rows <= 0) return
        val cx = (center.x * cols).toInt().coerceIn(0, cols - 1)
        val cy = (center.y * rows).toInt().coerceIn(0, rows - 1)
        val radiusCells = (radiusNorm * min(cols, rows)).toInt().coerceAtLeast(2)

        for (dy in -radiusCells..radiusCells) {
            for (dx in -radiusCells..radiusCells) {
                val x = cx + dx
                val y = cy + dy
                if (x !in 0 until cols || y !in 0 until rows) continue
                val dist = hypot(dx.toFloat(), dy.toFloat())
                if (dist > radiusCells) continue
                val falloff = 1f - (dist / radiusCells).coerceIn(0f, 1f)
                val idx = index(x, y)
                cells[idx] = (cells[idx] - falloff * 0.42f).coerceAtLeast(0f)
            }
        }
    }

    fun eraseStroke(from: Offset, to: Offset, radiusNorm: Float) {
        val distance = hypot(
            (to.x - from.x).toDouble(),
            (to.y - from.y).toDouble()
        ).toFloat()
        val steps = (distance / CloudAndClaritySessionConfig.ERASE_SAMPLE_STEP_NORM)
            .toInt()
            .coerceAtLeast(1)
        for (step in 0..steps) {
            val t = step.toFloat() / steps
            val point = Offset(
                from.x + (to.x - from.x) * t,
                from.y + (to.y - from.y) * t
            )
            eraseNormalized(point, radiusNorm)
        }
    }

    fun copyCells(): FloatArray = cells.copyOf()

    companion object {
        fun fullFog(cols: Int, rows: Int): RevealMask =
            RevealMask(cols, rows, FloatArray(cols * rows) { 1f })

        fun fromCells(cols: Int, rows: Int, cells: FloatArray): RevealMask =
            RevealMask(cols, rows, cells)
    }

    private fun index(col: Int, row: Int): Int = row * cols + col
}
