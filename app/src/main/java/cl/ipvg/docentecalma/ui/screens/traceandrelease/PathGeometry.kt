package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

internal object PathGeometry {

    fun polylineLength(points: List<Offset>): Float {
        if (points.size < 2) return 0f
        var length = 0f
        for (i in 1 until points.size) {
            length += (points[i] - points[i - 1]).getDistance()
        }
        return length
    }

    fun resamplePolyline(points: List<Offset>, targetCount: Int): List<Offset> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1 || targetCount <= 1) return listOf(points.first())

        val totalLength = polylineLength(points)
        if (totalLength <= 0f) return listOf(points.first())

        val spacing = totalLength / (targetCount - 1).coerceAtLeast(1)
        val result = mutableListOf<Offset>()
        result.add(points.first())

        var segmentStart = 0
        var distanceAlong = 0f
        var nextSampleAt = spacing

        while (segmentStart < points.lastIndex && result.size < targetCount) {
            val from = points[segmentStart]
            val to = points[segmentStart + 1]
            val segmentLength = (to - from).getDistance()
            if (segmentLength <= 0f) {
                segmentStart++
                continue
            }

            while (nextSampleAt <= distanceAlong + segmentLength && result.size < targetCount) {
                val t = ((nextSampleAt - distanceAlong) / segmentLength).coerceIn(0f, 1f)
                result.add(
                    Offset(
                        from.x + (to.x - from.x) * t,
                        from.y + (to.y - from.y) * t
                    )
                )
                nextSampleAt += spacing
            }

            distanceAlong += segmentLength
            segmentStart++
        }

        if (result.last() != points.last()) {
            result.add(points.last())
        }

        return result
    }

    fun pointAtProgress(resampled: List<Offset>, progress: Float): Offset {
        if (resampled.isEmpty()) return Offset.Zero
        if (resampled.size == 1) return resampled.first()

        val t = progress.coerceIn(0f, 1f)
        val totalLength = polylineLength(resampled)
        if (totalLength <= 0f) return resampled.first()

        val targetDistance = totalLength * t
        var traversed = 0f

        for (i in 1 until resampled.size) {
            val from = resampled[i - 1]
            val to = resampled[i]
            val segmentLength = (to - from).getDistance()
            if (segmentLength <= 0f) continue

            if (traversed + segmentLength >= targetDistance) {
                val localT = ((targetDistance - traversed) / segmentLength).coerceIn(0f, 1f)
                return Offset(
                    from.x + (to.x - from.x) * localT,
                    from.y + (to.y - from.y) * localT
                )
            }
            traversed += segmentLength
        }

        return resampled.last()
    }

    fun facingRightAtProgress(resampled: List<Offset>, progress: Float): Boolean {
        if (resampled.size < 2) return true

        val t = progress.coerceIn(0f, 1f)
        val index = ((resampled.size - 1) * t).toInt().coerceIn(0, resampled.lastIndex - 1)
        val from = resampled[index]
        val to = resampled[(index + 1).coerceAtMost(resampled.lastIndex)]
        return to.x >= from.x
    }

    fun easeInOutCubic(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        return if (clamped < 0.5f) {
            4f * clamped * clamped * clamped
        } else {
            val f = -2f * clamped + 2f
            1f - (f * f * f) / 2f
        }
    }
}

private fun Offset.getDistance(): Float {
    val dx = x
    val dy = y
    return sqrt(dx * dx + dy * dy)
}
