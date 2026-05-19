package cl.ipvg.docentecalma.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
import cl.ipvg.docentecalma.ui.theme.IpvgBlueDeep
import cl.ipvg.docentecalma.ui.theme.IpvgBluePrimary
import cl.ipvg.docentecalma.ui.theme.IpvgYellow
import cl.ipvg.docentecalma.ui.theme.OnIpvgBlueDeep

/**
 * Hero institucional de bienvenida: saludo, mensaje de acompañamiento,
 * mascota con presencia clara y chips de contexto IPVG.
 */
@Composable
fun BrandingHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val heroBrush = Brush.linearGradient(
        colors = listOf(
            IpvgBlueDeep,
            IpvgBluePrimary,
            IpvgBlueDeep.copy(alpha = 0.92f)
        )
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(heroBrush)
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Instituto Profesional Virginio Gómez",
            style = MaterialTheme.typography.labelSmall,
            color = OnIpvgBlueDeep.copy(alpha = 0.72f),
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnIpvgBlueDeep
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnIpvgBlueDeep.copy(alpha = 0.88f)
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 124.dp, height = 132.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.04f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Mascot(
                        state = MascotState.Idle,
                        contentDescription = "Mascota ${MascotPersona.NAME}, identidad visual de Docente Calma",
                        sizeDp = 108.dp,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InstitutionChip(
                text = "IP Virginio Gómez",
                background = IpvgYellow,
                foreground = IpvgBluePrimary
            )
        }
    }
}

@Composable
private fun InstitutionChip(
    text: String,
    background: Color,
    foreground: Color,
    borderColor: Color? = null
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                }
            )
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = foreground
        )
    }
}
