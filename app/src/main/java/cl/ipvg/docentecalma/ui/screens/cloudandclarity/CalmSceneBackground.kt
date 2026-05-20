package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

internal fun DrawScope.drawCalmSceneBackground(
    vitality: Float,
    clockMs: Long,
    colors: CloudAndClarityColors = CloudAndClarityTheme.palette,
    layout: CloudSceneLayout = CloudSceneCatalog.layout(CloudSceneVariant.MorningBreeze)
) {
    val v = vitality.coerceIn(0f, 1f)
    val warmBoost = v * 0.18f
    val pulse = 0.5f + 0.5f * sin(clockMs / 2400.0).toFloat()

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.skyTop.copy(alpha = 0.72f + warmBoost),
                colors.skyBottom.copy(alpha = 0.88f + warmBoost * 0.5f),
                colors.horizonGlow.copy(alpha = 0.25f + v * 0.35f)
            ),
            startY = 0f,
            endY = size.height
        ),
        size = size
    )

    val sunCenter = Offset(
        size.width * layout.sunAnchorX,
        size.height * (layout.sunAnchorY - v * 0.02f)
    )
    val sunRadius = size.minDimension * (0.09f + v * 0.025f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.sunHalo.copy(alpha = 0.15f + v * 0.25f),
                colors.sunHalo.copy(alpha = 0f)
            ),
            center = sunCenter,
            radius = sunRadius * 3.2f
        ),
        radius = sunRadius * 3.2f,
        center = sunCenter
    )
    drawCircle(
        color = colors.sunCore.copy(alpha = 0.55f + v * 0.4f),
        radius = sunRadius * (0.92f + pulse * 0.04f),
        center = sunCenter
    )

    drawHill(
        baseY = size.height * layout.farHillBaseY,
        amplitude = size.height * 0.08f,
        color = colors.hillFar.copy(alpha = 0.35f + v * 0.2f),
        phase = layout.hillPhaseOffset
    )
    drawHill(
        baseY = size.height * layout.nearHillBaseY,
        amplitude = size.height * 0.1f,
        color = colors.hillNear.copy(alpha = 0.4f + v * 0.22f),
        phase = layout.hillPhaseOffset + 1.4f
    )

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.meadow.copy(alpha = 0.35f + v * 0.45f),
                colors.meadow.copy(alpha = 0.55f + v * 0.35f)
            ),
            startY = size.height * 0.72f,
            endY = size.height
        ),
        topLeft = Offset(0f, size.height * 0.72f),
        size = Size(size.width, size.height * 0.28f)
    )

    if (v > 0.35f) {
        val sparkleCount = (4 + v * 4).toInt()
        repeat(sparkleCount) { index ->
            val x = size.width * (0.15f + index * 0.17f)
            val y = size.height * (0.35f + (index % 3) * 0.06f)
            val alpha = (0.08f + v * 0.18f) * (0.6f + pulse * 0.4f)
            drawCircle(
                color = colors.sunCore.copy(alpha = alpha),
                radius = 2f + index * 0.4f,
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawHill(
    baseY: Float,
    amplitude: Float,
    color: androidx.compose.ui.graphics.Color,
    phase: Float
) {
    val path = Path().apply {
        moveTo(0f, size.height)
        val segments = 8
        for (segment in 0..segments) {
            val x = size.width * segment / segments
            val wave = sin((segment / segments.toFloat()) * 3.2f + phase).toFloat()
            val y = baseY + wave * amplitude
            lineTo(x, y)
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

internal fun DrawScope.drawCalmSceneGlow(
    vitality: Float,
    clockMs: Long,
    colors: CloudAndClarityColors = CloudAndClarityTheme.palette
) {
    val v = vitality.coerceIn(0f, 1f)
    if (v <= 0.05f) return
    val pulse = 0.5f + 0.5f * sin(clockMs / 1800.0).toFloat()
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.horizonGlow.copy(alpha = 0.12f * v * pulse),
                colors.horizonGlow.copy(alpha = 0f)
            ),
            center = Offset(size.width * 0.5f, size.height * 0.78f),
            radius = size.minDimension * 0.65f
        ),
        size = size
    )
}
