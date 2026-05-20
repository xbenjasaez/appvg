package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class TraceAndReleaseViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TraceAndReleaseUiState())
    val uiState: StateFlow<TraceAndReleaseUiState> = _uiState.asStateFlow()

    private var fadeTickerJob: Job? = null
    private var clearPulseJob: Job? = null
    private var walkJob: Job? = null
    private var autoCloseJob: Job? = null
    private var nextStrokeId = 0L
    private var nextParticleId = 0L
    private var nextSproutId = 0L
    private var lastSproutSampleOffset: Offset? = null
    private var distanceSinceLastSprout = 0f

    fun onEvent(event: TraceAndReleaseEvent) {
        when (event) {
            TraceAndReleaseEvent.OnStart -> startPlaying()
            TraceAndReleaseEvent.OnClear -> clearCanvas()
            TraceAndReleaseEvent.OnExit -> resetToIntro()
            TraceAndReleaseEvent.OnRequestClose -> requestClose()
            TraceAndReleaseEvent.OnContinuePlaying -> continuePlaying()
            TraceAndReleaseEvent.OnFinishGoBack -> resetToIntro()
            is TraceAndReleaseEvent.OnDragStart -> beginStroke(event.offset)
            is TraceAndReleaseEvent.OnDrag -> extendStroke(event.offset)
            TraceAndReleaseEvent.OnDragEnd -> finishStroke()
        }
    }

    private fun startPlaying() {
        walkJob?.cancel()
        autoCloseJob?.cancel()
        _uiState.value = TraceAndReleaseUiState(
            screenPhase = TraceScreenPhase.Playing,
            clockMs = now()
        )
        lastSproutSampleOffset = null
        distanceSinceLastSprout = 0f
        startFadeTicker()
    }

    private fun requestClose() {
        walkJob?.cancel()
        autoCloseJob?.cancel()
        fadeTickerJob?.cancel()
        _uiState.update { it.copy(screenPhase = TraceScreenPhase.Finished, virgiWalk = null) }
    }

    private fun continuePlaying() {
        autoCloseJob?.cancel()
        _uiState.update {
            it.copy(
                screenPhase = TraceScreenPhase.Playing,
                clockMs = now(),
                interactionMode = TraceInteractionMode.Drawing,
                virgiWalk = null
            )
        }
        startFadeTicker()
    }

    private fun resetToIntro() {
        walkJob?.cancel()
        autoCloseJob?.cancel()
        fadeTickerJob?.cancel()
        clearPulseJob?.cancel()
        _uiState.value = TraceAndReleaseUiState()
        nextStrokeId = 0L
        nextParticleId = 0L
        nextSproutId = 0L
        lastSproutSampleOffset = null
        distanceSinceLastSprout = 0f
    }

    private fun clearCanvas() {
        walkJob?.cancel()
        autoCloseJob?.cancel()
        lastSproutSampleOffset = null
        distanceSinceLastSprout = 0f
        _uiState.update {
            it.copy(
                strokes = emptyList(),
                activeStroke = null,
                pathSprouts = emptyList(),
                touchParticles = emptyList(),
                clearAcknowledged = true,
                clockMs = now(),
                interactionMode = TraceInteractionMode.Drawing,
                virgiWalk = null
            )
        }
        clearPulseJob?.cancel()
        clearPulseJob = viewModelScope.launch {
            delay(1_200L)
            _uiState.update { state ->
                if (state.screenPhase == TraceScreenPhase.Playing) {
                    state.copy(clearAcknowledged = false)
                } else {
                    state
                }
            }
        }
    }

    private fun beginStroke(offset: Offset) {
        if (!_uiState.value.isInputEnabled) return
        val timestamp = now()
        val stroke = TraceStroke(
            id = nextStrokeId++,
            points = listOf(TracePoint(offset, timestamp))
        )
        lastSproutSampleOffset = offset
        distanceSinceLastSprout = 0f
        _uiState.update {
            it.copy(
                activeStroke = stroke,
                clockMs = timestamp,
                clearAcknowledged = false
            )
        }
        spawnParticles(listOf(offset), stroke.id.toInt())
        maybeSpawnSproutAt(offset, stroke.id.toInt())
    }

    private fun extendStroke(offset: Offset) {
        val timestamp = now()
        _uiState.update { state ->
            if (!state.isInputEnabled) return@update state
            val active = state.activeStroke ?: return@update state
            val last = active.points.lastOrNull()?.offset
            if (last != null && (last - offset).getDistance() < MIN_POINT_DISTANCE) {
                return@update state.copy(clockMs = timestamp)
            }

            if (last != null) {
                distanceSinceLastSprout += (last - offset).getDistance()
            }

            val updated = active.copy(
                points = active.points + TracePoint(offset, timestamp)
            )
            val particles = spawnParticlesForPoints(
                existing = state.touchParticles,
                offsets = listOf(offset),
                tintIndex = active.id.toInt()
            )

            var sprouts = state.pathSprouts
            if (distanceSinceLastSprout >= TraceSessionConfig.SPROUT_SAMPLE_DISTANCE_PX) {
                sprouts = addSprout(sprouts, offset, active.id.toInt())
                distanceSinceLastSprout = 0f
                lastSproutSampleOffset = offset
            }

            state.copy(
                activeStroke = updated,
                clockMs = timestamp,
                touchParticles = particles,
                pathSprouts = sprouts
            )
        }
    }

    private fun finishStroke() {
        var strokeToWalk: TraceStroke? = null
        var guidedCountAfter = 0

        _uiState.update { state ->
            if (state.screenPhase != TraceScreenPhase.Playing) {
                return@update state.copy(activeStroke = null, clockMs = now())
            }
            val active = state.activeStroke
            if (active == null) {
                return@update state.copy(clockMs = now())
            }

            val rawOffsets = active.points.map { it.offset }
            val resampled = PathGeometry.resamplePolyline(
                rawOffsets,
                TraceSessionConfig.RESAMPLE_TARGET_COUNT
            )
            val qualifies = strokeQualifiesForGuide(active)
            val newGuidedCount = if (
                qualifies &&
                state.guidedStrokesCompleted < TraceSessionConfig.GUIDED_STROKES
            ) {
                state.guidedStrokesCompleted + 1
            } else {
                state.guidedStrokesCompleted
            }
            guidedCountAfter = newGuidedCount

            val completedStroke = active.copy(resampledPath = resampled)
            var sprouts = state.pathSprouts
            if (resampled.size >= 2) {
                val burstIndices = listOf(
                    resampled.size / 4,
                    resampled.size / 2,
                    resampled.size * 3 / 4
                ).distinct()
                burstIndices.forEach { index ->
                    sprouts = addSprout(
                        sprouts,
                        resampled[index.coerceIn(0, resampled.lastIndex)],
                        active.id.toInt() + index
                    )
                }
            }

            val particles = spawnParticlesForPoints(
                existing = state.touchParticles,
                offsets = active.points.takeLast(3).map { it.offset },
                tintIndex = active.id.toInt()
            )

            val shouldWalk = qualifies && resampled.size >= 2
            if (shouldWalk) {
                strokeToWalk = completedStroke
            }

            state.copy(
                strokes = state.strokes + completedStroke,
                activeStroke = null,
                guidedStrokesCompleted = newGuidedCount,
                touchParticles = particles,
                pathSprouts = sprouts,
                clockMs = now(),
                interactionMode = if (shouldWalk) {
                    TraceInteractionMode.VirgiWalking
                } else {
                    TraceInteractionMode.Drawing
                }
            )
        }

        strokeToWalk?.let { startVirgiWalk(it, guidedCountAfter) }
    }

    private fun startVirgiWalk(stroke: TraceStroke, guidedCount: Int) {
        walkJob?.cancel()
        val path = stroke.resampledPath
        if (path.size < 2) return

        val startPosition = path.first()
        val initialWalk = VirgiPathWalk(
            strokeId = stroke.id,
            resampledPath = path,
            progress = 0f,
            position = startPosition,
            facingRight = PathGeometry.facingRightAtProgress(path, 0f)
        )

        _uiState.update {
            it.copy(
                virgiWalk = initialWalk,
                interactionMode = TraceInteractionMode.VirgiWalking
            )
        }

        walkJob = viewModelScope.launch {
            val startMs = now()
            val duration = TraceSessionConfig.VIRGI_WALK_DURATION_MS.toFloat()

            while (isActive) {
                delay(TraceSessionConfig.VIRGI_WALK_TICK_MS)
                val elapsed = (now() - startMs).toFloat()
                val linearProgress = (elapsed / duration).coerceIn(0f, 1f)
                val easedProgress = PathGeometry.easeInOutCubic(linearProgress)
                val position = PathGeometry.pointAtProgress(path, easedProgress)
                val facingRight = PathGeometry.facingRightAtProgress(path, easedProgress)

                _uiState.update { state ->
                    state.copy(
                        virgiWalk = VirgiPathWalk(
                            strokeId = stroke.id,
                            resampledPath = path,
                            progress = easedProgress,
                            position = position,
                            facingRight = facingRight
                        ),
                        clockMs = now()
                    )
                }

                if (linearProgress >= 1f) break
            }

            completeVirgiWalk(stroke.id, guidedCount)
        }
    }

    private fun completeVirgiWalk(strokeId: Long, guidedCount: Int) {
        _uiState.update { state ->
            state.copy(
                strokes = state.strokes.map { stroke ->
                    if (stroke.id == strokeId) stroke.copy(walkCompleted = true) else stroke
                },
                virgiWalk = null,
                interactionMode = TraceInteractionMode.Drawing,
                clockMs = now()
            )
        }

        if (guidedCount >= TraceSessionConfig.GUIDED_STROKES) {
            scheduleAutoClose()
        }
    }

    private fun scheduleAutoClose() {
        autoCloseJob?.cancel()
        autoCloseJob = viewModelScope.launch {
            delay(TraceSessionConfig.AUTO_CLOSE_DELAY_MS)
            fadeTickerJob?.cancel()
            _uiState.update {
                if (it.screenPhase == TraceScreenPhase.Playing) {
                    it.copy(screenPhase = TraceScreenPhase.Finished, virgiWalk = null)
                } else {
                    it
                }
            }
        }
    }

    private fun maybeSpawnSproutAt(offset: Offset, seed: Int) {
        _uiState.update { state ->
            state.copy(pathSprouts = addSprout(state.pathSprouts, offset, seed))
        }
    }

    private fun addSprout(
        existing: List<PathSprout>,
        offset: Offset,
        seed: Int
    ): List<PathSprout> {
        val kinds = SproutKind.entries
        val kind = kinds[(seed + existing.size) % kinds.size]
        val rotation = ((seed * 37) % 360).toFloat()
        val sprout = PathSprout(
            id = nextSproutId++,
            offset = offset,
            bornMs = now(),
            kind = kind,
            rotationDeg = rotation
        )
        return (existing + sprout).takeLast(TraceSessionConfig.MAX_SPROUTS)
    }

    private fun strokeQualifiesForGuide(stroke: TraceStroke): Boolean {
        if (stroke.points.size >= TraceSessionConfig.MIN_GUIDED_POINTS) return true
        var length = 0f
        for (i in 1 until stroke.points.size) {
            length += (stroke.points[i].offset - stroke.points[i - 1].offset).getDistance()
        }
        return length >= MIN_GUIDED_PATH_LENGTH
    }

    private fun spawnParticles(offsets: List<Offset>, tintIndex: Int) {
        _uiState.update { state ->
            state.copy(touchParticles = spawnParticlesForPoints(state.touchParticles, offsets, tintIndex))
        }
    }

    private fun spawnParticlesForPoints(
        existing: List<TraceParticle>,
        offsets: List<Offset>,
        tintIndex: Int
    ): List<TraceParticle> {
        if (offsets.isEmpty()) return existing
        val timestamp = now()
        val spawned = offsets.flatMap { offset ->
            List(PARTICLES_PER_SAMPLE) { index ->
                val angle = (index * 72f + tintIndex * 17f) * (Math.PI.toFloat() / 180f)
                val radius = 4f + index * 2.5f
                TraceParticle(
                    id = nextParticleId++,
                    offset = Offset(
                        offset.x + kotlin.math.cos(angle) * radius,
                        offset.y + kotlin.math.sin(angle) * radius
                    ),
                    bornMs = timestamp,
                    tintIndex = tintIndex
                )
            }
        }
        return (existing + spawned).takeLast(TraceSessionConfig.MAX_PARTICLES)
    }

    private fun pruneParticles(particles: List<TraceParticle>, currentMs: Long): List<TraceParticle> {
        return particles.filter { particle ->
            particleAlpha(particle.bornMs, currentMs) > 0.04f
        }
    }

    private fun pruneSprouts(sprouts: List<PathSprout>, currentMs: Long): List<PathSprout> {
        return sprouts.filter { sprout ->
            sproutAlpha(sprout.bornMs, currentMs) > 0.04f
        }
    }

    private fun startFadeTicker() {
        fadeTickerJob?.cancel()
        fadeTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(FADE_TICK_MS)
                val current = now()
                _uiState.update { state ->
                    if (state.screenPhase != TraceScreenPhase.Playing) return@update state
                    val prunedStrokes = state.strokes.mapNotNull { stroke ->
                        val visible = stroke.points.any { point ->
                            alphaFor(point.timestampMs, current, stroke.walkCompleted) > 0.02f
                        }
                        if (visible) stroke else null
                    }
                    state.copy(
                        strokes = prunedStrokes,
                        touchParticles = pruneParticles(state.touchParticles, current),
                        pathSprouts = pruneSprouts(state.pathSprouts, current),
                        clockMs = current
                    )
                }
            }
        }
    }

    private fun now(): Long = System.currentTimeMillis()

    companion object {
        const val FADE_DURATION_MS = 9_000L
        const val FADE_TICK_MS = 48L
        private const val MIN_POINT_DISTANCE = 6f
        private const val MIN_GUIDED_PATH_LENGTH = 80f
        private const val PARTICLES_PER_SAMPLE = 4

        fun alphaFor(
            pointTimestampMs: Long,
            nowMs: Long,
            walkCompleted: Boolean = false
        ): Float {
            val duration = if (walkCompleted) {
                TraceSessionConfig.WALKED_FADE_DURATION_MS.toFloat()
            } else {
                FADE_DURATION_MS.toFloat()
            }
            val age = (nowMs - pointTimestampMs).coerceAtLeast(0L)
            return (1f - age / duration).coerceIn(0f, 1f)
        }

        fun particleAlpha(bornMs: Long, nowMs: Long): Float {
            val age = (nowMs - bornMs).coerceAtLeast(0L)
            return (1f - age.toFloat() / TraceSessionConfig.PARTICLE_LIFETIME_MS).coerceIn(0f, 1f)
        }

        fun sproutAlpha(bornMs: Long, nowMs: Long): Float {
            val age = (nowMs - bornMs).coerceAtLeast(0L)
            val growth = (age.toFloat() / TraceSessionConfig.SPROUT_GROWTH_MS).coerceIn(0f, 1f)
            val growthFactor = PathGeometry.easeInOutCubic(growth)
            val decay = (age.toFloat() / TraceSessionConfig.SPROUT_LIFETIME_MS).coerceIn(0f, 1f)
            val fadeOut = (1f - decay * 0.65f).coerceIn(0f, 1f)
            return (growthFactor * fadeOut).coerceIn(0f, 1f)
        }
    }

    override fun onCleared() {
        fadeTickerJob?.cancel()
        clearPulseJob?.cancel()
        walkJob?.cancel()
        autoCloseJob?.cancel()
        super.onCleared()
    }
}

private fun Offset.getDistance(): Float {
    val dx = x
    val dy = y
    return sqrt(dx * dx + dy * dy)
}
