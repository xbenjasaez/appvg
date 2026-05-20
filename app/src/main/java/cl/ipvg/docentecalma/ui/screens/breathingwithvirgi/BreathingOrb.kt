package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val OrbBaseSize = 240.dp
private val OrbHaloSize = 268.dp

@Composable
internal fun BreathingOrb(
    phase: BreathingPhase,
    cycle: Int,
    phaseColors: BreathingPhaseColors,
    secondsRemaining: Int,
    phaseLabel: String,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(phase.startScale) }

    LaunchedEffect(phase, cycle) {
        scaleAnim.snapTo(phase.startScale)
        scaleAnim.animateTo(
            targetValue = phase.endScale,
            animationSpec = tween(
                durationMillis = phase.animationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier
            .semantics {
                contentDescription = BreathingCopy.orbContentDescription(
                    phaseLabel = phaseLabel,
                    secondsRemaining = secondsRemaining
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scaleAnim.value * 1.12f)
                .size(OrbHaloSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            phaseColors.haloInner,
                            phaseColors.haloOuter
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .scale(scaleAnim.value)
                .size(OrbBaseSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            phaseColors.orbCenter,
                            phaseColors.orbEdge
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = phaseColors.orbBorder,
                    shape = CircleShape
                )
        )
    }
}
