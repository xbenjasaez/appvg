package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import cl.ipvg.docentecalma.ui.theme.IpvgBlueSoft
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import cl.ipvg.docentecalma.ui.theme.IpvgYellowSoft

@Immutable
internal data class CloudAndClarityColors(
    val skyTop: Color,
    val skyBottom: Color,
    val horizonGlow: Color,
    val hillFar: Color,
    val hillNear: Color,
    val meadow: Color,
    val sunCore: Color,
    val sunHalo: Color,
    val fogLayer: Color,
    val fogHighlight: Color,
    val mistParticle: Color,
    val hudBackground: Color,
    val hudText: Color,
    val hudHint: Color
)

internal object CloudAndClarityTheme {
    val palette = CloudAndClarityColors(
        skyTop = Color(0xFF8EBCE8),
        skyBottom = Color(0xFFE8F4FC),
        horizonGlow = IpvgYellowSoft.copy(alpha = 0.55f),
        hillFar = IpvgBlueSoft.copy(alpha = 0.38f),
        hillNear = IpvgBlueVirginio.copy(alpha = 0.28f),
        meadow = IpvgGreenSoft.copy(alpha = 0.72f),
        sunCore = Color(0xFFFFF6E0),
        sunHalo = Color(0xFFFFE4B5).copy(alpha = 0.45f),
        fogLayer = Color(0xFFF4F7FB),
        fogHighlight = Color.White.copy(alpha = 0.55f),
        mistParticle = Color.White.copy(alpha = 0.35f),
        hudBackground = Color(0xFF0F2138).copy(alpha = 0.42f),
        hudText = Color.White,
        hudHint = Color.White.copy(alpha = 0.72f)
    )

    val colors: CloudAndClarityColors
        @Composable get() = remember {
            CloudAndClarityColors(
                skyTop = Color(0xFF8EBCE8),
                skyBottom = Color(0xFFE8F4FC),
                horizonGlow = IpvgYellowSoft.copy(alpha = 0.55f),
                hillFar = IpvgBlueSoft.copy(alpha = 0.38f),
                hillNear = IpvgBlueVirginio.copy(alpha = 0.28f),
                meadow = IpvgGreenSoft.copy(alpha = 0.72f),
                sunCore = Color(0xFFFFF6E0),
                sunHalo = Color(0xFFFFE4B5).copy(alpha = 0.45f),
                fogLayer = Color(0xFFF4F7FB),
                fogHighlight = Color.White.copy(alpha = 0.55f),
                mistParticle = Color.White.copy(alpha = 0.35f),
                hudBackground = Color(0xFF0F2138).copy(alpha = 0.42f),
                hudText = Color.White,
                hudHint = Color.White.copy(alpha = 0.72f)
            )
        }
}
