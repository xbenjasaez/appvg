package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.sin

@Composable
fun TranquilLightPlayfield(
    lights: List<FloatingLight>,
    particles: List<LightParticle>,
    lanternGlow: Float,
    sceneVitality: Float,
    lanternPulse: Float,
    clockMs: Long,
    isInputEnabled: Boolean,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onSizeChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val canvasModifier = if (isInputEnabled) {
        modifier
            .fillMaxSize()
            .onSizeChanged { size: IntSize ->
                onSizeChanged(Offset(size.width.toFloat(), size.height.toFloat()))
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDrag = { change, _ -> onDrag(change.position) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
    } else {
        modifier
            .fillMaxSize()
            .onSizeChanged { size: IntSize ->
                onSizeChanged(Offset(size.width.toFloat(), size.height.toFloat()))
            }
    }

    val colors = TranquilLightTheme.colors
    val lanternCenterNorm = TranquilLightSessionConfig.LANTERN_CENTER

    Canvas(modifier = canvasModifier) {
        val lanternCenter = Offset(
            lanternCenterNorm.x * size.width,
            lanternCenterNorm.y * size.height
        )
        val vitality = sceneVitality.coerceIn(0f, 1f)
        val effectiveGlow = (lanternGlow + lanternPulse * 0.15f).coerceIn(0f, 1.2f)

        drawNightGardenBackground(
            vitality = vitality,
            lanternCenter = lanternCenter,
            clockMs = clockMs,
            colors = colors
        )

        particles
            .filter { it.kind == ParticleKind.Ambient }
            .forEach { particle ->
                val alpha = particle.alphaAt(clockMs)
                if (alpha <= 0f) return@forEach
                val pos = particle.positionAt(clockMs)
                val px = Offset(pos.x * size.width, pos.y * size.height)
                drawLightParticle(position = px, alpha = alpha, kind = ParticleKind.Ambient, colors = colors)
            }

        drawLanternCore(
            center = lanternCenter,
            glow = effectiveGlow,
            clockMs = clockMs,
            colors = colors
        )

        lights.forEach { light ->
            if (light.state == LightMotionState.Collected) return@forEach
            val normalized = TranquilLightSessionConfig.effectiveOffset(light)
            val position = Offset(normalized.x * size.width, normalized.y * size.height)
            val pulse = when (light.state) {
                LightMotionState.Dragging -> 1.15f
                LightMotionState.Traveling -> 1f + light.travelProgress * 0.12f
                else -> 1f + sin(light.driftPhase * 2f) * 0.06f
            }
            drawFireflyLight(
                light = light,
                position = position,
                pulse = pulse,
                clockMs = clockMs,
                colors = colors
            )
        }

        particles
            .filter { it.kind != ParticleKind.Ambient }
            .forEach { particle ->
                val alpha = particle.alphaAt(clockMs)
                if (alpha <= 0f) return@forEach
                val pos = particle.positionAt(clockMs)
                val px = Offset(pos.x * size.width, pos.y * size.height)
                drawLightParticle(position = px, alpha = alpha, kind = particle.kind, colors = colors)
            }
    }
}
