package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

internal fun DrawScope.drawLanternCore(
    center: Offset,
    glow: Float,
    clockMs: Long = 0L,
    colors: TranquilLightColors = TranquilLightTheme.colors
) {
    val baseRadius = size.minDimension * 0.135f
    val bodyHeight = baseRadius * 2.5f
    val bodyWidth = baseRadius * 1.4f
    val glowClamped = glow.coerceIn(0f, 1.2f)

    val subtlePulse = if (glowClamped > 0.5f && clockMs > 0L) {
        sin(clockMs / 1800.0).toFloat() * 0.03f
    } else {
        0f
    }
    val effectiveGlow = glowClamped + subtlePulse

    val bodyTop = center.y - bodyHeight * 0.38f
    val bodyBottom = center.y + bodyHeight * 0.52f

    if (effectiveGlow > 0.02f) {
        val outerGlowRadius = baseRadius * (2.2f + effectiveGlow * 1.4f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.lanternGlowHalo.copy(alpha = 0.15f * effectiveGlow),
                    colors.lanternGlowCore.copy(alpha = 0.06f * effectiveGlow),
                    Color.Transparent
                ),
                center = center,
                radius = outerGlowRadius
            ),
            radius = outerGlowRadius,
            center = center
        )
    }

    val capPath = Path().apply {
        moveTo(center.x - bodyWidth * 0.22f, bodyTop - baseRadius * 0.2f)
        quadraticTo(center.x, bodyTop - baseRadius * 0.55f, center.x + bodyWidth * 0.22f, bodyTop - baseRadius * 0.2f)
        lineTo(center.x + bodyWidth * 0.18f, bodyTop - baseRadius * 0.05f)
        lineTo(center.x - bodyWidth * 0.18f, bodyTop - baseRadius * 0.05f)
        close()
    }
    drawPath(path = capPath, color = colors.lanternBody.copy(alpha = 0.92f))

    drawLine(
        color = colors.lanternBody.copy(alpha = 0.85f),
        start = Offset(center.x, bodyTop - baseRadius * 0.65f),
        end = Offset(center.x, bodyTop - baseRadius * 0.25f),
        strokeWidth = baseRadius * 0.1f
    )

    val lanternPath = Path().apply {
        moveTo(center.x - bodyWidth * 0.12f, bodyTop - baseRadius * 0.05f)
        lineTo(center.x + bodyWidth * 0.12f, bodyTop - baseRadius * 0.05f)
        lineTo(center.x + bodyWidth * 0.52f, bodyTop + baseRadius * 0.08f)
        lineTo(center.x + bodyWidth * 0.55f, bodyBottom)
        quadraticTo(center.x, bodyBottom + baseRadius * 0.22f, center.x - bodyWidth * 0.55f, bodyBottom)
        lineTo(center.x - bodyWidth * 0.52f, bodyTop + baseRadius * 0.08f)
        close()
    }

    val bodyAlpha = 0.8f + effectiveGlow * 0.15f
    drawPath(path = lanternPath, color = colors.lanternBody.copy(alpha = bodyAlpha))

    val glassCenter = Offset(center.x, center.y + baseRadius * 0.08f)
    val glassRadius = baseRadius * 0.78f

    if (effectiveGlow > 0.01f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.lanternGlowCore.copy(alpha = 0.5f * effectiveGlow),
                    colors.lanternGlowHalo.copy(alpha = 0.18f * effectiveGlow),
                    Color.Transparent
                ),
                center = glassCenter,
                radius = glassRadius * (1.8f + effectiveGlow * 0.9f)
            ),
            radius = glassRadius * (1.8f + effectiveGlow * 0.9f),
            center = glassCenter
        )
    }

    val glassWarmth = 0.3f + effectiveGlow * 0.55f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.lanternGlass.copy(alpha = glassWarmth),
                colors.lanternBody.copy(alpha = 0.2f + effectiveGlow * 0.2f)
            ),
            center = glassCenter,
            radius = glassRadius
        ),
        radius = glassRadius,
        center = glassCenter
    )

    if (effectiveGlow > 0.05f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.lanternGlowCore.copy(alpha = 0.75f * effectiveGlow),
                    colors.lanternGlowCore.copy(alpha = 0f)
                ),
                center = glassCenter,
                radius = glassRadius * 0.5f
            ),
            radius = glassRadius * 0.5f,
            center = glassCenter
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.25f * effectiveGlow),
            radius = glassRadius * 0.18f,
            center = Offset(glassCenter.x - glassRadius * 0.25f, glassCenter.y - glassRadius * 0.3f)
        )
    }

    drawPath(
        path = lanternPath,
        color = colors.lanternBody.copy(alpha = 0.4f),
        style = Stroke(width = baseRadius * 0.05f)
    )
}

internal fun DrawScope.drawFireflyLight(
    light: FloatingLight,
    position: Offset,
    pulse: Float,
    clockMs: Long,
    colors: TranquilLightColors = TranquilLightTheme.colors
) {
    val baseCore = size.minDimension * 0.014f * light.sizeScale * pulse
    val organicPulse = 1f + sin(light.pulsePhase + clockMs / 900.0).toFloat() * 0.08f
    val coreRadius = baseCore * organicPulse
    val haloRadiusX = coreRadius * 3.4f * 1.12f
    val haloRadiusY = coreRadius * 3.2f

    val warmCore = Color(
        red = (colors.lightCore.red * light.warmth).coerceIn(0f, 1f),
        green = (colors.lightCore.green * light.warmth).coerceIn(0f, 1f),
        blue = (colors.lightCore.blue * (0.95f + light.warmth * 0.05f)).coerceIn(0f, 1f),
        alpha = colors.lightCore.alpha
    )

    val haloAlpha = when (light.state) {
        LightMotionState.Dragging -> 0.42f
        LightMotionState.Traveling -> 0.35f
        else -> 0.28f
    }

    drawCircle(
        color = colors.lightHalo.copy(alpha = haloAlpha),
        radius = haloRadiusY,
        center = position
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                warmCore.copy(alpha = 0.9f),
                colors.lightHalo.copy(alpha = haloAlpha * 0.7f),
                colors.lightHalo.copy(alpha = 0f)
            ),
            center = position,
            radius = haloRadiusX
        ),
        radius = haloRadiusX,
        center = position
    )
    drawCircle(
        color = warmCore,
        radius = coreRadius,
        center = position
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = coreRadius * 0.35f,
        center = Offset(position.x - coreRadius * 0.2f, position.y - coreRadius * 0.2f)
    )
}

internal fun DrawScope.drawFloatingLight(
    position: Offset,
    colors: TranquilLightColors = TranquilLightTheme.colors,
    pulse: Float = 1f,
    sizeScale: Float = 1f,
    warmth: Float = 1f
) {
    val light = FloatingLight(
        id = -1,
        baseOffset = position,
        sizeScale = sizeScale,
        warmth = warmth
    )
    drawFireflyLight(light, position, pulse, clockMs = 0L, colors = colors)
}

internal fun DrawScope.drawLightParticle(
    position: Offset,
    alpha: Float,
    kind: ParticleKind = ParticleKind.Burst,
    colors: TranquilLightColors = TranquilLightTheme.colors
) {
    if (alpha <= 0f) return
    val baseRadius = when (kind) {
        ParticleKind.Burst -> size.minDimension * 0.008f
        ParticleKind.Trail -> size.minDimension * 0.005f
        ParticleKind.Ambient -> size.minDimension * 0.004f
    }
    val radius = baseRadius * (0.8f + alpha * 0.6f)

    drawCircle(
        color = TranquilLightSessionConfig.particleColor.copy(alpha = alpha * 0.75f),
        radius = radius * 1.8f,
        center = position
    )
    drawCircle(
        color = colors.lightCore.copy(alpha = alpha * 0.85f),
        radius = radius,
        center = position
    )
    if (kind == ParticleKind.Burst) {
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.4f),
            radius = radius * 0.45f,
            center = position
        )
    }
}
