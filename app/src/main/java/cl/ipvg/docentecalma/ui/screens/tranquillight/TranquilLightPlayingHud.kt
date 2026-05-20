package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun TranquilLightPlayingHud(
    progressLabel: String,
    collectedCount: Int,
    totalLights: Int,
    modifier: Modifier = Modifier
) {
    val colors = TranquilLightTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedContent(
            targetState = progressLabel,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tranquilLightProgressHud"
        ) { label ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.hudBackground.copy(alpha = 0.72f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colors.hudText.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .semantics { contentDescription = label }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(totalLights) { index ->
                            val filled = index < collectedCount
                            Box(
                                modifier = Modifier
                                    .size(if (filled) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) colors.hudDotActive
                                        else colors.hudDotInactive.copy(alpha = 0.6f)
                                    )
                            )
                        }
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.hudText
                    )
                }
            }
        }

        if (collectedCount == 0) {
            Text(
                text = TranquilLightCopy.playingHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.hudHint.copy(alpha = 0.55f)
            )
        }
    }
}
