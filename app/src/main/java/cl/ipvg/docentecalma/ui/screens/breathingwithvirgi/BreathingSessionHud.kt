package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun BreathingSessionHud(
    secondsRemaining: Int,
    phaseLabel: String,
    cycleLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val a11yDescription = BreathingCopy.sessionHudContentDescription(
        phaseLabel = phaseLabel,
        secondsRemaining = secondsRemaining,
        cycleLabel = cycleLabel
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11yDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedContent(
            targetState = secondsRemaining,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 2 }).togetherWith(
                    fadeOut() + slideOutVertically { -it / 2 }
                )
            },
            label = "breathing_seconds"
        ) { seconds ->
            Text(
                text = seconds.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = accentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = cycleLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
