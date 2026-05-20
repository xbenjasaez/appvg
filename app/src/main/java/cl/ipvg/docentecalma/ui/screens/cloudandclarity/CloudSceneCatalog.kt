package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cl.ipvg.docentecalma.ui.theme.IpvgBlueSoft
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import cl.ipvg.docentecalma.ui.theme.IpvgOrangeSoft
import cl.ipvg.docentecalma.ui.theme.IpvgYellowSoft

enum class CloudSceneVariant {
    MorningBreeze,
    GoldenHour,
    VirginioHills,
    QuietMeadow
}

@Immutable
internal data class CloudSceneLayout(
    val sunAnchorX: Float,
    val sunAnchorY: Float,
    val hillPhaseOffset: Float,
    val farHillBaseY: Float,
    val nearHillBaseY: Float
)

internal object CloudSceneCatalog {
    val rotation: List<CloudSceneVariant> = CloudSceneVariant.entries

    fun nextVariant(after: CloudSceneVariant?): CloudSceneVariant {
        val list = rotation
        if (after == null) return list.first()
        val index = list.indexOf(after)
        return list[(index + 1) % list.size]
    }

    fun colors(variant: CloudSceneVariant): CloudAndClarityColors = when (variant) {
        CloudSceneVariant.MorningBreeze -> CloudAndClarityColors(
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
        CloudSceneVariant.GoldenHour -> CloudAndClarityColors(
            skyTop = Color(0xFF9BB8E8),
            skyBottom = Color(0xFFFFF0D8),
            horizonGlow = IpvgOrangeSoft.copy(alpha = 0.65f),
            hillFar = Color(0xFFC9A87A).copy(alpha = 0.35f),
            hillNear = Color(0xFFB8885A).copy(alpha = 0.32f),
            meadow = IpvgYellowSoft.copy(alpha = 0.78f),
            sunCore = Color(0xFFFFF8E8),
            sunHalo = IpvgOrangeSoft.copy(alpha = 0.5f),
            fogLayer = Color(0xFFFFF5E8),
            fogHighlight = Color.White.copy(alpha = 0.5f),
            mistParticle = Color(0xFFFFF0E0).copy(alpha = 0.4f),
            hudBackground = Color(0xFF3D2400).copy(alpha = 0.38f),
            hudText = Color.White,
            hudHint = Color.White.copy(alpha = 0.75f)
        )
        CloudSceneVariant.VirginioHills -> CloudAndClarityColors(
            skyTop = Color(0xFF5A9FD4),
            skyBottom = Color(0xFFD4E8F8),
            horizonGlow = IpvgBlueSoft.copy(alpha = 0.5f),
            hillFar = IpvgBlueVirginio.copy(alpha = 0.32f),
            hillNear = IpvgBlueSoft.copy(alpha = 0.42f),
            meadow = Color(0xFFB8D4E8).copy(alpha = 0.65f),
            sunCore = Color(0xFFE8F4FF),
            sunHalo = IpvgBlueVirginio.copy(alpha = 0.35f),
            fogLayer = Color(0xFFE8F0F8),
            fogHighlight = Color.White.copy(alpha = 0.52f),
            mistParticle = Color(0xFFE0EEF8).copy(alpha = 0.38f),
            hudBackground = Color(0xFF001E4D).copy(alpha = 0.45f),
            hudText = Color.White,
            hudHint = Color.White.copy(alpha = 0.72f)
        )
        CloudSceneVariant.QuietMeadow -> CloudAndClarityColors(
            skyTop = Color(0xFFA8D4B8),
            skyBottom = Color(0xFFE8F6EC),
            horizonGlow = IpvgGreenSoft.copy(alpha = 0.6f),
            hillFar = Color(0xFF7AB89A).copy(alpha = 0.34f),
            hillNear = Color(0xFF5A9A7A).copy(alpha = 0.3f),
            meadow = IpvgGreenSoft.copy(alpha = 0.85f),
            sunCore = Color(0xFFFFFBE8),
            sunHalo = IpvgGreenSoft.copy(alpha = 0.4f),
            fogLayer = Color(0xFFF0F8F2),
            fogHighlight = Color.White.copy(alpha = 0.54f),
            mistParticle = Color(0xFFE8F4EA).copy(alpha = 0.36f),
            hudBackground = Color(0xFF1A3D28).copy(alpha = 0.4f),
            hudText = Color.White,
            hudHint = Color.White.copy(alpha = 0.72f)
        )
    }

    fun layout(variant: CloudSceneVariant): CloudSceneLayout = when (variant) {
        CloudSceneVariant.MorningBreeze -> CloudSceneLayout(
            sunAnchorX = 0.72f,
            sunAnchorY = 0.22f,
            hillPhaseOffset = 0f,
            farHillBaseY = 0.62f,
            nearHillBaseY = 0.7f
        )
        CloudSceneVariant.GoldenHour -> CloudSceneLayout(
            sunAnchorX = 0.78f,
            sunAnchorY = 0.28f,
            hillPhaseOffset = 0.8f,
            farHillBaseY = 0.64f,
            nearHillBaseY = 0.72f
        )
        CloudSceneVariant.VirginioHills -> CloudSceneLayout(
            sunAnchorX = 0.62f,
            sunAnchorY = 0.18f,
            hillPhaseOffset = 1.6f,
            farHillBaseY = 0.58f,
            nearHillBaseY = 0.68f
        )
        CloudSceneVariant.QuietMeadow -> CloudSceneLayout(
            sunAnchorX = 0.5f,
            sunAnchorY = 0.34f,
            hillPhaseOffset = 2.4f,
            farHillBaseY = 0.66f,
            nearHillBaseY = 0.74f
        )
    }
}
