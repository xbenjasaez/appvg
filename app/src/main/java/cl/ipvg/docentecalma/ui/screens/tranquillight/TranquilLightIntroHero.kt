package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

@Composable
fun TranquilLightIntroHero(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lanternCenter = Offset(size.width * 0.5f, size.height * 0.55f)
        val clockMs = System.currentTimeMillis()
        val colors = TranquilLightTheme.colors

        drawNightGardenBackground(
            vitality = 0.25f,
            lanternCenter = lanternCenter,
            clockMs = clockMs,
            colors = colors
        )

        drawLanternCore(center = lanternCenter, glow = 0.35f, clockMs = clockMs, colors = colors)

        val orbPositions = listOf(
            Offset(size.width * 0.22f, size.height * 0.38f) to 0.9f,
            Offset(size.width * 0.38f, size.height * 0.22f) to 1.05f,
            Offset(size.width * 0.72f, size.height * 0.32f) to 0.95f,
            Offset(size.width * 0.82f, size.height * 0.52f) to 1.1f,
            Offset(size.width * 0.18f, size.height * 0.58f) to 0.88f
        )
        orbPositions.forEach { (pos, scale) ->
            drawFloatingLight(
                position = pos,
                pulse = 1f,
                sizeScale = scale,
                warmth = scale
            )
        }
    }
}
