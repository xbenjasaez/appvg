package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
@Composable
internal fun CloudRevealPlayfield(
    sceneVariant: CloudSceneVariant,
    maskCells: FloatArray,
    sceneVitality: Float,
    clockMs: Long,
    mistSeed: Int,
    erasePulse: Float,
    lastErasePx: Offset?,
    isInputEnabled: Boolean,
    onEraseStart: (Offset) -> Unit,
    onErase: (Offset) -> Unit,
    onEraseEnd: () -> Unit,
    onSizeChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CloudSceneCatalog.colors(sceneVariant)
    val layout = CloudSceneCatalog.layout(sceneVariant)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                onSizeChanged(Offset(size.width.toFloat(), size.height.toFloat()))
            }
            .pointerInput(isInputEnabled) {
                if (!isInputEnabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset -> onEraseStart(offset) },
                    onDrag = { change, _ ->
                        change.consume()
                        onErase(change.position)
                    },
                    onDragEnd = { onEraseEnd() },
                    onDragCancel = { onEraseEnd() }
                )
            }
    ) {
        drawCalmSceneBackground(
            vitality = sceneVitality,
            clockMs = clockMs,
            colors = colors,
            layout = layout
        )
        drawCalmSceneGlow(
            vitality = sceneVitality,
            clockMs = clockMs,
            colors = colors
        )
        drawCloudFogFromMask(
            maskCells = maskCells,
            cols = CloudAndClaritySessionConfig.MASK_COLS,
            rows = CloudAndClaritySessionConfig.MASK_ROWS,
            clockMs = clockMs,
            colors = colors
        )
        drawAmbientMist(
            vitality = sceneVitality,
            clockMs = clockMs,
            seed = mistSeed,
            colors = colors
        )
        lastErasePx?.let { center ->
            drawEraseShimmer(center = center, strength = erasePulse, colors = colors)
        }
    }
}
