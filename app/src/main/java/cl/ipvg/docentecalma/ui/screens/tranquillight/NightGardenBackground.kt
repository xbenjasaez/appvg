package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

internal fun DrawScope.drawNightGardenBackground(
    vitality: Float,
    lanternCenter: Offset,
    clockMs: Long,
    colors: TranquilLightColors = TranquilLightTheme.colors
) {
    val w = size.width
    val h = size.height
    val minDim = size.minDimension

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.backgroundDeep,
                colors.backgroundTop,
                colors.backgroundMid,
                colors.backgroundBottom
            ),
            startY = 0f,
            endY = h
        )
    )

    val bloomRadius = minDim * (0.38f + vitality * 0.12f)
    val bloomAlpha = 0.06f + vitality * 0.22f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.lanternGlowHalo.copy(alpha = bloomAlpha),
                colors.lanternGlowCore.copy(alpha = bloomAlpha * 0.4f),
                colors.backgroundMid.copy(alpha = 0f)
            ),
            center = lanternCenter,
            radius = bloomRadius
        ),
        radius = bloomRadius,
        center = lanternCenter
    )

    val mistAlpha = 0.04f + vitality * 0.04f
    listOf(0.35f, 0.62f).forEach { yFrac ->
        val mistY = h * yFrac
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.mist.copy(alpha = mistAlpha),
                    colors.mist.copy(alpha = 0f)
                ),
                center = Offset(w * 0.5f, mistY),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(w * 0.5f, mistY)
        )
    }

    val timeSec = clockMs / 1000f
    val activeSeeds = (ambientSeeds.size * (0.5f + vitality * 0.5f)).toInt()
        .coerceIn(8, ambientSeeds.size)

    ambientSeeds.take(activeSeeds).forEach { seed ->
        val driftX = sin(timeSec * 0.4f + seed.phase) * minDim * 0.008f
        val driftY = sin(timeSec * 0.3f + seed.phase * 1.2f) * minDim * 0.006f
        val pos = Offset(
            seed.x * w + driftX,
            seed.y * h + driftY
        )
        val radius = minDim * 0.0035f * seed.size * (0.85f + vitality * 0.3f)
        val alpha = (0.12f + vitality * 0.18f) * (0.7f + sin(timeSec + seed.phase) * 0.3f)
        drawCircle(
            color = colors.lightCore.copy(alpha = alpha.coerceIn(0.05f, 0.35f)),
            radius = radius,
            center = pos
        )
        drawCircle(
            color = colors.lightHalo.copy(alpha = alpha * 0.35f),
            radius = radius * 2.5f,
            center = pos
        )
    }

    drawMascotGround(colors, vitality)

    val vignetteRadius = kotlin.math.hypot(w.toDouble(), h.toDouble()).toFloat() * 0.72f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.vignette.copy(alpha = 0f),
                colors.vignette.copy(alpha = 0.25f + vitality * 0.08f),
                colors.vignette.copy(alpha = 0.45f)
            ),
            center = Offset(w * 0.5f, h * 0.48f),
            radius = vignetteRadius
        ),
        radius = vignetteRadius,
        center = Offset(w * 0.5f, h * 0.48f)
    )
}

internal fun DrawScope.drawMascotGround(
    colors: TranquilLightColors = TranquilLightTheme.colors,
    vitality: Float = 0f
) {
    val w = size.width
    val h = size.height
    val groundTop = h * 0.76f

    val hillPath = Path().apply {
        moveTo(0f, h)
        lineTo(0f, groundTop + h * 0.04f)
        quadraticTo(w * 0.25f, groundTop - h * 0.02f, w * 0.5f, groundTop)
        quadraticTo(w * 0.78f, groundTop + h * 0.03f, w, groundTop + h * 0.06f)
        lineTo(w, h)
        close()
    }

    drawPath(
        path = hillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                colors.groundHighlight.copy(alpha = 0.35f + vitality * 0.15f),
                colors.groundDark
            ),
            startY = groundTop,
            endY = h
        )
    )

    val pedestalCenter = Offset(w * 0.5f, h * 0.84f)
    val pedestalWidth = w * 0.28f
    val pedestalHeight = h * 0.04f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.lanternGlowHalo.copy(alpha = 0.08f + vitality * 0.12f),
                colors.groundDark.copy(alpha = 0f)
            ),
            center = pedestalCenter,
            radius = pedestalWidth * 0.9f
        ),
        radius = pedestalWidth * 0.9f,
        center = pedestalCenter
    )

    val shadowPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = pedestalCenter.x - pedestalWidth * 0.5f,
                top = pedestalCenter.y - pedestalHeight * 0.3f,
                right = pedestalCenter.x + pedestalWidth * 0.5f,
                bottom = pedestalCenter.y + pedestalHeight * 0.5f
            )
        )
    }
    drawPath(
        path = shadowPath,
        color = colors.vignette.copy(alpha = 0.35f)
    )
}

private val ambientSeeds = TranquilLightSessionConfig.ambientSeeds
