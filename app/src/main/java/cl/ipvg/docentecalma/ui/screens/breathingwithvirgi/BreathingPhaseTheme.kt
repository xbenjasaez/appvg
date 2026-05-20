package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

import androidx.compose.ui.graphics.Color
import cl.ipvg.docentecalma.ui.theme.IpvgBluePrimary
import cl.ipvg.docentecalma.ui.theme.IpvgBlueSoft
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGrayBrand
import cl.ipvg.docentecalma.ui.theme.IpvgGrayCool
import cl.ipvg.docentecalma.ui.theme.IpvgGreen
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import cl.ipvg.docentecalma.ui.theme.OnPrimaryContainerLight
import cl.ipvg.docentecalma.ui.theme.OnSecondaryContainerLight
import cl.ipvg.docentecalma.ui.theme.PrimaryContainerLight
import cl.ipvg.docentecalma.ui.theme.SecondaryContainerLight

internal data class BreathingPhaseColors(
    val haloOuter: Color,
    val haloInner: Color,
    val orbCenter: Color,
    val orbEdge: Color,
    val orbBorder: Color,
    val accent: Color,
    val onAccent: Color,
    val progressTrack: Color
)

internal object BreathingPhaseTheme {
    private val RestTint = Color(0xFFD4DCE6)

    internal fun colorsFor(phase: BreathingPhase): BreathingPhaseColors = when (phase) {
        BreathingPhase.Inhale -> BreathingPhaseColors(
            haloOuter = IpvgBlueSoft.copy(alpha = 0.18f),
            haloInner = SecondaryContainerLight.copy(alpha = 0.55f),
            orbCenter = SecondaryContainerLight,
            orbEdge = IpvgBlueSoft.copy(alpha = 0.75f),
            orbBorder = IpvgBlueVirginio.copy(alpha = 0.45f),
            accent = IpvgBlueVirginio,
            onAccent = OnSecondaryContainerLight,
            progressTrack = SecondaryContainerLight.copy(alpha = 0.5f)
        )
        BreathingPhase.Hold -> BreathingPhaseColors(
            haloOuter = IpvgBluePrimary.copy(alpha = 0.12f),
            haloInner = PrimaryContainerLight.copy(alpha = 0.65f),
            orbCenter = PrimaryContainerLight,
            orbEdge = IpvgBluePrimary.copy(alpha = 0.35f),
            orbBorder = IpvgBluePrimary.copy(alpha = 0.4f),
            accent = IpvgBluePrimary,
            onAccent = OnPrimaryContainerLight,
            progressTrack = PrimaryContainerLight.copy(alpha = 0.55f)
        )
        BreathingPhase.Exhale -> BreathingPhaseColors(
            haloOuter = IpvgGreen.copy(alpha = 0.08f),
            haloInner = IpvgGreenSoft.copy(alpha = 0.7f),
            orbCenter = IpvgGreenSoft,
            orbEdge = IpvgGreen.copy(alpha = 0.25f),
            orbBorder = IpvgGreen.copy(alpha = 0.35f),
            accent = IpvgGreen,
            onAccent = Color(0xFF0D3D1C),
            progressTrack = IpvgGreenSoft.copy(alpha = 0.6f)
        )
        BreathingPhase.Rest -> BreathingPhaseColors(
            haloOuter = IpvgGrayCool.copy(alpha = 0.5f),
            haloInner = RestTint.copy(alpha = 0.85f),
            orbCenter = RestTint,
            orbEdge = IpvgGrayCool,
            orbBorder = IpvgGrayBrand.copy(alpha = 0.25f),
            accent = IpvgGrayBrand,
            onAccent = Color(0xFF3D4A56),
            progressTrack = IpvgGrayCool.copy(alpha = 0.65f)
        )
    }
}
