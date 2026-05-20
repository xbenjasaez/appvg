package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import cl.ipvg.docentecalma.ui.theme.IpvgYellow
import cl.ipvg.docentecalma.ui.theme.IpvgYellowSoft
import kotlin.math.cos
import kotlin.math.sin

object TranquilLightSessionConfig {
    const val TOTAL_LIGHTS = 7
    const val FLOAT_TICK_MS = 60L
    const val TRAVEL_DURATION_MS = 1400L
    const val LANTERN_GLOW_MS = 600L
    const val PARTICLE_TICK_MS = 50L
    const val AMBIENT_TICK_MS = 2500L
    const val MAX_PARTICLES = 24
    const val DRIFT_SPEED = 0.04f
    const val DRIFT_AMPLITUDE = 0.018f
    const val LIGHT_HIT_RADIUS_FRACTION = 0.09f
    const val MIN_CENTER_DISTANCE = 0.22f
    const val MIN_LIGHT_DISTANCE = 0.12f
    const val LANTERN_PULSE_DECAY_MS = 400L
    const val TRAIL_SPAWN_INTERVAL_MS = 80L
    const val BURST_PARTICLE_COUNT = 6

    val LANTERN_CENTER = Offset(0.5f, 0.46f)
    const val SPAWN_RADIUS = 0.36f
    const val MAX_SPAWN_Y = 0.72f

    val nightBackgroundDeep = Color(0xFF060D18)
    val nightBackgroundTop = Color(0xFF0B1424)
    val nightBackgroundMid = Color(0xFF0F1A2E)
    val nightBackgroundBottom = Color(0xFF1A2840)
    val mistColor = Color(0xFFB8C8E8)
    val vignetteColor = Color(0xFF02060E)
    val groundDark = Color(0xFF0A1220)
    val groundHighlight = Color(0xFF152238)
    val lightWarmCore = Color(0xFFFFE8B8)
    val lightHalo = IpvgYellow
    val lanternBody = Color(0xFF2A3D5C)
    val lanternGlassDim = Color(0xFF3D5270)
    val lanternGlowCore = IpvgYellow
    val lanternGlowHalo = IpvgYellowSoft
    val particleColor = Color(0xFFFFF2D6)
    val hudBackground = Color(0xFF0D1525)
    val hudText = Color(0xFFE8EDF5)
    val hudHint = Color(0xFFB0BDD0)
    val hudDotActive = IpvgYellow
    val hudDotInactive = Color(0xFF3A4A62)

    private val sizeScales = floatArrayOf(0.88f, 1.05f, 0.94f, 1.12f, 0.9f, 1.08f, 0.96f)
    private val warmthValues = floatArrayOf(0.92f, 1.02f, 0.95f, 1.05f, 0.9f, 1.0f, 0.98f)
    private val driftMultipliers = floatArrayOf(0.85f, 1.15f, 0.95f, 1.2f, 0.78f, 1.05f, 0.92f)

    private val spawnAngles = floatArrayOf(
        0.35f, 1.05f, 1.75f, 2.45f, 3.25f, 4.05f, 4.85f
    )

    data class AmbientSeed(val x: Float, val y: Float, val phase: Float, val size: Float)

    val ambientSeeds: List<AmbientSeed> = buildList {
        val positions = listOf(
            0.08f to 0.22f, 0.22f to 0.15f, 0.38f to 0.12f, 0.55f to 0.18f,
            0.72f to 0.14f, 0.88f to 0.24f, 0.15f to 0.35f, 0.85f to 0.32f,
            0.12f to 0.48f, 0.9f to 0.45f, 0.25f to 0.58f, 0.75f to 0.55f,
            0.18f to 0.68f, 0.82f to 0.65f, 0.42f to 0.28f, 0.62f to 0.38f,
            0.3f to 0.42f, 0.7f to 0.5f, 0.5f to 0.25f, 0.35f to 0.52f,
            0.65f to 0.62f, 0.48f to 0.62f, 0.2f to 0.78f, 0.8f to 0.75f,
            0.1f to 0.55f, 0.92f to 0.52f, 0.45f to 0.38f, 0.58f to 0.48f
        )
        positions.forEachIndexed { index, (x, y) ->
            add(
                AmbientSeed(
                    x = x,
                    y = y,
                    phase = index * 0.7f,
                    size = 0.7f + (index % 4) * 0.15f
                )
            )
        }
    }

    fun spawnLights(): List<FloatingLight> {
        return spawnAngles.mapIndexed { index, angle ->
            val x = LANTERN_CENTER.x + cos(angle) * SPAWN_RADIUS
            val y = LANTERN_CENTER.y + sin(angle) * SPAWN_RADIUS * 0.88f
            FloatingLight(
                id = index,
                baseOffset = Offset(
                    x.coerceIn(0.1f, 0.9f),
                    y.coerceIn(0.12f, MAX_SPAWN_Y)
                ),
                driftPhase = index * 0.9f,
                sizeScale = sizeScales[index % sizeScales.size],
                warmth = warmthValues[index % warmthValues.size],
                pulsePhase = index * 1.3f,
                driftMultiplier = driftMultipliers[index % driftMultipliers.size]
            )
        }
    }

    fun effectiveOffset(light: FloatingLight, center: Offset = LANTERN_CENTER): Offset {
        return when (light.state) {
            LightMotionState.Floating -> floatingOffset(light)
            LightMotionState.Dragging -> light.dragOffset?.let { clampNormalized(it) } ?: floatingOffset(light)
            LightMotionState.Traveling -> {
                val from = light.travelStartOffset ?: floatingOffset(light)
                val eased = travelEase(light.travelProgress.coerceIn(0f, 1f))
                travelPosition(from, center, eased)
            }
            LightMotionState.Collected -> center
        }
    }

    fun travelEase(t: Float): Float = FastOutSlowInEasing.transform(t.coerceIn(0f, 1f))

    fun travelPosition(from: Offset, to: Offset, t: Float): Offset {
        val mid = Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f)
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.001f)
        val perpX = -dy / len * 0.06f
        val perpY = dx / len * 0.06f
        val control = Offset(mid.x + perpX, mid.y + perpY)
        val u = 1f - t
        return Offset(
            u * u * from.x + 2f * u * t * control.x + t * t * to.x,
            u * u * from.y + 2f * u * t * control.y + t * t * to.y
        )
    }

    fun clampNormalized(offset: Offset): Offset =
        Offset(offset.x.coerceIn(0.05f, 0.95f), offset.y.coerceIn(0.08f, 0.92f))

    fun floatingOffset(light: FloatingLight): Offset {
        val amp = DRIFT_AMPLITUDE * light.driftMultiplier
        val driftX = sin(light.driftPhase) * amp
        val driftY = cos(light.driftPhase * 0.85f) * amp
        return clampNormalized(
            Offset(light.baseOffset.x + driftX, light.baseOffset.y + driftY)
        )
    }
}
