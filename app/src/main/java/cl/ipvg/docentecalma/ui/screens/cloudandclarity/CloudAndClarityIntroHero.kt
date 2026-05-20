package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

@Composable
fun CloudAndClarityIntroHero(
    modifier: Modifier = Modifier,
    sceneVariant: CloudSceneVariant = CloudSceneVariant.MorningBreeze
) {
    val colors = CloudSceneCatalog.colors(sceneVariant)
    val layout = CloudSceneCatalog.layout(sceneVariant)
    Canvas(modifier = modifier) {
        drawCalmSceneBackground(
            vitality = 0.35f,
            clockMs = System.currentTimeMillis(),
            colors = colors,
            layout = layout
        )
        drawCloudFogFromMask(
            maskCells = CloudAndClarityUiState.fullMaskSnapshot(),
            cols = CloudAndClaritySessionConfig.MASK_COLS,
            rows = CloudAndClaritySessionConfig.MASK_ROWS,
            clockMs = System.currentTimeMillis(),
            colors = colors
        )
        drawAmbientMist(
            vitality = 0.2f,
            clockMs = System.currentTimeMillis(),
            seed = 7,
            colors = colors
        )
        drawEraseShimmer(
            center = Offset(size.width * 0.38f, size.height * 0.48f),
            strength = 0.85f,
            colors = colors
        )
    }
}
