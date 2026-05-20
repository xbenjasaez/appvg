package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlin.random.Random

internal fun DrawScope.drawCloudFogFromMask(
    maskCells: FloatArray,
    cols: Int,
    rows: Int,
    clockMs: Long,
    colors: CloudAndClarityColors = CloudAndClarityTheme.palette
) {
    if (cols <= 0 || rows <= 0 || maskCells.size != cols * rows) return

    val step = 2
    val cellW = size.width / cols * step
    val cellH = size.height / rows * step
    val drift = sin(clockMs / 3200.0).toFloat() * cellW * 0.12f

    for (row in 0 until rows step step) {
        for (col in 0 until cols step step) {
            val density = sampleBlockDensity(maskCells, cols, rows, col, row, step)
            if (density < 0.04f) continue
            val cx = col * (size.width / cols) + cellW * 0.5f + drift * (row % 3 - 1) * 0.15f
            val cy = row * (size.height / rows) + cellH * 0.5f
            val radius = maxOf(cellW, cellH) * 0.78f
            val alpha = (density * 0.88f).coerceIn(0f, 0.92f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.fogHighlight.copy(alpha = alpha * 0.55f),
                        colors.fogLayer.copy(alpha = alpha * 0.82f),
                        colors.fogLayer.copy(alpha = 0f)
                    ),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun sampleBlockDensity(
    maskCells: FloatArray,
    cols: Int,
    rows: Int,
    col: Int,
    row: Int,
    step: Int
): Float {
    var sum = 0f
    var count = 0
    for (dy in 0 until step) {
        for (dx in 0 until step) {
            val x = col + dx
            val y = row + dy
            if (x >= cols || y >= rows) continue
            sum += maskCells[y * cols + x]
            count++
        }
    }
    return if (count == 0) 0f else sum / count
}

internal fun DrawScope.drawAmbientMist(
    vitality: Float,
    clockMs: Long,
    seed: Int,
    colors: CloudAndClarityColors = CloudAndClarityTheme.palette
) {
    val remainingFog = (1f - vitality).coerceIn(0f, 1f)
    if (remainingFog < 0.08f) return
    val random = Random(seed)
    val count = (6 + remainingFog * 10).toInt()
    repeat(count) {
        val baseX = random.nextFloat()
        val baseY = random.nextFloat() * 0.65f
        val driftX = sin((clockMs + it * 700L) / 2800.0).toFloat() * 0.04f
        val driftY = sin((clockMs + it * 1100L) / 3600.0).toFloat() * 0.02f
        val center = Offset(
            (baseX + driftX).coerceIn(0.05f, 0.95f) * size.width,
            (baseY + driftY).coerceIn(0.05f, 0.75f) * size.height
        )
        val radius = size.minDimension * (0.06f + random.nextFloat() * 0.08f)
        val alpha = remainingFog * 0.12f * (0.65f + random.nextFloat() * 0.35f)
        drawCircle(
            color = colors.mistParticle.copy(alpha = alpha),
            radius = radius,
            center = center
        )
    }
}

internal fun DrawScope.drawEraseShimmer(
    center: Offset,
    strength: Float,
    colors: CloudAndClarityColors = CloudAndClarityTheme.palette
) {
    if (strength <= 0f) return
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.sunCore.copy(alpha = 0.22f * strength),
                colors.fogHighlight.copy(alpha = 0f)
            ),
            center = center,
            radius = size.minDimension * 0.09f * strength
        ),
        radius = size.minDimension * 0.09f * strength,
        center = center
    )
}
