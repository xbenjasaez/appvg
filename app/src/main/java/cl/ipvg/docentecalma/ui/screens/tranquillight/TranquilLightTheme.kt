package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class TranquilLightColors(
    val backgroundDeep: Color,
    val backgroundTop: Color,
    val backgroundMid: Color,
    val backgroundBottom: Color,
    val mist: Color,
    val vignette: Color,
    val groundDark: Color,
    val groundHighlight: Color,
    val lightCore: Color,
    val lightHalo: Color,
    val lanternBody: Color,
    val lanternGlass: Color,
    val lanternGlowCore: Color,
    val lanternGlowHalo: Color,
    val hudBackground: Color,
    val hudText: Color,
    val hudHint: Color,
    val hudDotActive: Color,
    val hudDotInactive: Color
)

object TranquilLightTheme {
    val colors = TranquilLightColors(
        backgroundDeep = TranquilLightSessionConfig.nightBackgroundDeep,
        backgroundTop = TranquilLightSessionConfig.nightBackgroundTop,
        backgroundMid = TranquilLightSessionConfig.nightBackgroundMid,
        backgroundBottom = TranquilLightSessionConfig.nightBackgroundBottom,
        mist = TranquilLightSessionConfig.mistColor,
        vignette = TranquilLightSessionConfig.vignetteColor,
        groundDark = TranquilLightSessionConfig.groundDark,
        groundHighlight = TranquilLightSessionConfig.groundHighlight,
        lightCore = TranquilLightSessionConfig.lightWarmCore,
        lightHalo = TranquilLightSessionConfig.lightHalo,
        lanternBody = TranquilLightSessionConfig.lanternBody,
        lanternGlass = TranquilLightSessionConfig.lanternGlassDim,
        lanternGlowCore = TranquilLightSessionConfig.lanternGlowCore,
        lanternGlowHalo = TranquilLightSessionConfig.lanternGlowHalo,
        hudBackground = TranquilLightSessionConfig.hudBackground,
        hudText = TranquilLightSessionConfig.hudText,
        hudHint = TranquilLightSessionConfig.hudHint,
        hudDotActive = TranquilLightSessionConfig.hudDotActive,
        hudDotInactive = TranquilLightSessionConfig.hudDotInactive
    )

    val lanternCenterNormalized = TranquilLightSessionConfig.LANTERN_CENTER

    fun nightBackgroundBrush(): Brush =
        Brush.verticalGradient(
            colors = listOf(
                colors.backgroundDeep,
                colors.backgroundTop,
                colors.backgroundMid,
                colors.backgroundBottom
            )
        )
}
