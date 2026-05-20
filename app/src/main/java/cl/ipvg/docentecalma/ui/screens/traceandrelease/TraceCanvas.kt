package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.ui.theme.IpvgBlueSoft
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGreen
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun TraceCanvas(
    strokes: List<TraceStroke>,
    activeStroke: TraceStroke?,
    particles: List<TraceParticle>,
    pathSprouts: List<PathSprout>,
    clockMs: Long,
    isInputEnabled: Boolean,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val baseStrokeWidth = with(density) { 10.dp.toPx() }
    val rippleRadius = with(density) { 28.dp.toPx() }
    val particleBaseRadius = with(density) { 3.dp.toPx() }

    val activeStrokeId = activeStroke?.id

    val allStrokes = buildList {
        addAll(strokes)
        activeStroke?.let { add(it) }
    }

    val canvasModifier = if (isInputEnabled) {
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDrag = { change, _ -> onDrag(change.position) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
    } else {
        modifier.fillMaxSize()
    }

    Canvas(modifier = canvasModifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8F2FA),
                    Color(0xFFF4F8FC),
                    Color(0xFFF5F0E8),
                    Color(0xFFEDF5F0)
                )
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    IpvgBlueVirginio.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.22f, size.height * 0.18f),
                radius = size.minDimension * 0.45f
            ),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.22f, size.height * 0.18f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    IpvgGreenSoft.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.82f, size.height * 0.72f),
                radius = size.minDimension * 0.38f
            ),
            radius = size.minDimension * 0.38f,
            center = Offset(size.width * 0.82f, size.height * 0.72f)
        )

        pathSprouts.forEach { sprout ->
            val alpha = TraceAndReleaseViewModel.sproutAlpha(sprout.bornMs, clockMs)
            if (alpha < 0.04f) return@forEach
            drawSprout(sprout, alpha, baseStrokeWidth)
        }

        particles.forEach { particle ->
            val alpha = TraceAndReleaseViewModel.particleAlpha(particle.bornMs, clockMs)
            if (alpha < 0.04f) return@forEach
            val tint = if (particle.tintIndex % 2 == 0) IpvgBlueVirginio else IpvgGreen
            drawCircle(
                color = tint.copy(alpha = alpha * 0.55f),
                radius = particleBaseRadius * (0.7f + alpha * 0.5f),
                center = particle.offset
            )
        }

        allStrokes.forEachIndexed { strokeIndex, stroke ->
            val points = stroke.points
            if (points.isEmpty()) return@forEachIndexed

            val strokeTint = if (strokeIndex % 2 == 0) IpvgBlueVirginio else IpvgGreen
            val walked = stroke.walkCompleted

            for (i in 1 until points.size) {
                val from = points[i - 1]
                val to = points[i]
                val segmentAlpha = (
                    TraceAndReleaseViewModel.alphaFor(from.timestampMs, clockMs, walked) +
                        TraceAndReleaseViewModel.alphaFor(to.timestampMs, clockMs, walked)
                    ) / 2f
                if (segmentAlpha < 0.03f) continue

                val width = baseStrokeWidth * (0.65f + segmentAlpha * 0.55f)
                drawLine(
                    color = strokeTint.copy(alpha = segmentAlpha * 0.72f),
                    start = from.offset,
                    end = to.offset,
                    strokeWidth = width,
                    cap = StrokeCap.Round
                )
            }

            if (points.size == 1) {
                val point = points.first()
                val alpha = TraceAndReleaseViewModel.alphaFor(point.timestampMs, clockMs, walked)
                if (alpha > 0.03f) {
                    drawCircle(
                        color = strokeTint.copy(alpha = alpha * 0.5f),
                        radius = baseStrokeWidth * 0.45f * alpha,
                        center = point.offset
                    )
                }
            } else if (points.size >= 2) {
                val path = Path()
                val first = points.first()
                path.moveTo(first.offset.x, first.offset.y)
                for (i in 1 until points.lastIndex) {
                    val current = points[i]
                    val next = points[i + 1]
                    val mid = Offset(
                        (current.offset.x + next.offset.x) / 2f,
                        (current.offset.y + next.offset.y) / 2f
                    )
                    path.quadraticBezierTo(
                        current.offset.x,
                        current.offset.y,
                        mid.x,
                        mid.y
                    )
                }
                path.lineTo(points.last().offset.x, points.last().offset.y)

                val pathAlpha = points.map {
                    TraceAndReleaseViewModel.alphaFor(it.timestampMs, clockMs, walked)
                }.average().toFloat()
                if (pathAlpha > 0.03f) {
                    drawPath(
                        path = path,
                        color = IpvgBlueSoft.copy(alpha = pathAlpha * 0.35f),
                        style = Stroke(
                            width = baseStrokeWidth * 1.15f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            val latest = points.last()
            val rippleAlpha = TraceAndReleaseViewModel.alphaFor(latest.timestampMs, clockMs, walked)
            if (rippleAlpha > 0.05f && stroke.id == activeStrokeId) {
                drawCircle(
                    color = strokeTint.copy(alpha = rippleAlpha * 0.18f),
                    radius = rippleRadius * rippleAlpha,
                    center = latest.offset,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = strokeTint.copy(alpha = rippleAlpha * 0.1f),
                    radius = rippleRadius * 1.35f * rippleAlpha,
                    center = latest.offset,
                    style = Stroke(width = 1.5f)
                )
                drawCircle(
                    color = strokeTint.copy(alpha = rippleAlpha * 0.06f),
                    radius = rippleRadius * 1.75f * rippleAlpha,
                    center = latest.offset,
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}

private fun DrawScope.drawSprout(sprout: PathSprout, alpha: Float, baseStrokeWidth: Float) {
    val scale = 0.55f + alpha * 0.65f
    rotate(sprout.rotationDeg, sprout.offset) {
        when (sprout.kind) {
            SproutKind.Leaf -> drawLeaf(sprout.offset, alpha, scale, baseStrokeWidth)
            SproutKind.Bud -> drawBud(sprout.offset, alpha, scale, baseStrokeWidth)
            SproutKind.Bloom -> drawBloom(sprout.offset, alpha, scale, baseStrokeWidth)
            SproutKind.Sparkle -> drawSparkle(sprout.offset, alpha, scale, baseStrokeWidth)
        }
    }
}

private fun DrawScope.drawLeaf(center: Offset, alpha: Float, scale: Float, baseStrokeWidth: Float) {
    val w = baseStrokeWidth * 0.55f * scale
    val h = baseStrokeWidth * 0.9f * scale
    drawOval(
        color = IpvgGreen.copy(alpha = alpha * 0.55f),
        topLeft = Offset(center.x - w / 2f, center.y - h / 2f),
        size = androidx.compose.ui.geometry.Size(w, h)
    )
    drawLine(
        color = IpvgGreen.copy(alpha = alpha * 0.35f),
        start = Offset(center.x, center.y + h * 0.2f),
        end = Offset(center.x, center.y - h * 0.45f),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawBud(center: Offset, alpha: Float, scale: Float, baseStrokeWidth: Float) {
    val radius = baseStrokeWidth * 0.35f * scale
    drawLine(
        color = IpvgGreen.copy(alpha = alpha * 0.4f),
        start = Offset(center.x, center.y + radius * 1.2f),
        end = Offset(center.x, center.y - radius * 0.3f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = IpvgGreenSoft.copy(alpha = alpha * 0.65f),
        radius = radius,
        center = Offset(center.x, center.y - radius * 0.5f)
    )
}

private fun DrawScope.drawBloom(center: Offset, alpha: Float, scale: Float, baseStrokeWidth: Float) {
    val petalRadius = baseStrokeWidth * 0.22f * scale
    val petalCount = 4
    for (i in 0 until petalCount) {
        val angle = (i * 90f + 45f) * (Math.PI.toFloat() / 180f)
        val px = center.x + cos(angle) * petalRadius * 0.9f
        val py = center.y + sin(angle) * petalRadius * 0.9f
        drawCircle(
            color = IpvgBlueSoft.copy(alpha = alpha * 0.5f),
            radius = petalRadius,
            center = Offset(px, py)
        )
    }
    drawCircle(
        color = IpvgGreenSoft.copy(alpha = alpha * 0.7f),
        radius = petalRadius * 0.55f,
        center = center
    )
}

private fun DrawScope.drawSparkle(center: Offset, alpha: Float, scale: Float, baseStrokeWidth: Float) {
    val radius = baseStrokeWidth * 0.2f * scale
    drawCircle(
        color = IpvgBlueVirginio.copy(alpha = alpha * 0.45f),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.35f),
        radius = radius * 0.45f,
        center = center
    )
}
