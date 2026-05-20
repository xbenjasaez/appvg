package cl.ipvg.docentecalma.ui.screens.tranquillight

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
import kotlin.math.hypot
import kotlin.random.Random

@HiltViewModel
class TranquilLightViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TranquilLightUiState())
    val uiState: StateFlow<TranquilLightUiState> = _uiState.asStateFlow()

    private var floatJob: Job? = null
    private var particleJob: Job? = null
    private var ambientJob: Job? = null
    private var pulseDecayJob: Job? = null
    private var travelJob: Job? = null
    private var finishJob: Job? = null
    private var nextParticleId = 0L
    private var draggingLightId: Int? = null
    private var lastTrailSpawnMs = 0L

    fun onEvent(event: TranquilLightEvent) {
        when (event) {
            TranquilLightEvent.OnStart -> startPlaying()
            TranquilLightEvent.OnExit -> exitSession()
            TranquilLightEvent.OnRepeat -> resetToIntro()
            TranquilLightEvent.OnFinishGoBack -> resetToIntro()
            is TranquilLightEvent.OnDragStart -> handleDragStart(event.offsetPx)
            is TranquilLightEvent.OnDrag -> handleDrag(event.offsetPx)
            TranquilLightEvent.OnDragEnd -> handleDragEnd()
            is TranquilLightEvent.OnPlayfieldSizeChanged -> {
                _uiState.update { it.copy(playfieldSize = event.sizePx) }
            }
        }
    }

    private fun startPlaying() {
        cancelJobs()
        draggingLightId = null
        lastTrailSpawnMs = 0L
        _uiState.value = TranquilLightUiState(
            screenPhase = TranquilLightScreenPhase.Playing,
            lights = TranquilLightSessionConfig.spawnLights(),
            clockMs = now()
        )
        startFloatTicker()
        startParticleTicker()
        startAmbientTicker()
        startPulseDecayTicker()
    }

    private fun exitSession() {
        cancelJobs()
        draggingLightId = null
        _uiState.value = TranquilLightUiState()
    }

    private fun resetToIntro() {
        cancelJobs()
        draggingLightId = null
        _uiState.value = TranquilLightUiState(screenPhase = TranquilLightScreenPhase.Intro)
    }

    private fun handleDragStart(offsetPx: Offset) {
        val state = _uiState.value
        if (!state.isInputEnabled) return
        val size = state.playfieldSize
        if (size.x <= 0f || size.y <= 0f) return

        val hitLight = findFloatingLightAt(offsetPx, size) ?: return
        draggingLightId = hitLight.id
        val normalized = TranquilLightSessionConfig.clampNormalized(
            Offset(offsetPx.x / size.x, offsetPx.y / size.y)
        )

        spawnBurst(normalized)

        _uiState.update { current ->
            current.copy(
                lights = current.lights.map { light ->
                    if (light.id == hitLight.id) {
                        light.copy(state = LightMotionState.Dragging, dragOffset = normalized)
                    } else {
                        light
                    }
                },
                clockMs = now()
            )
        }
    }

    private fun handleDrag(offsetPx: Offset) {
        val lightId = draggingLightId ?: return
        val size = _uiState.value.playfieldSize
        if (size.x <= 0f || size.y <= 0f) return

        val normalized = TranquilLightSessionConfig.clampNormalized(
            Offset(offsetPx.x / size.x, offsetPx.y / size.y)
        )

        _uiState.update { current ->
            current.copy(
                lights = current.lights.map { light ->
                    if (light.id == lightId && light.state == LightMotionState.Dragging) {
                        light.copy(dragOffset = normalized)
                    } else {
                        light
                    }
                },
                clockMs = now()
            )
        }
    }

    private fun handleDragEnd() {
        val lightId = draggingLightId ?: return
        draggingLightId = null

        val state = _uiState.value
        val light = state.lights.find { it.id == lightId } ?: return
        if (light.state != LightMotionState.Dragging) return

        val releasePos = light.dragOffset ?: TranquilLightSessionConfig.floatingOffset(light)
        spawnBurst(releasePos, count = 3)
        startTravel(lightId, releasePos)
    }

    private fun findFloatingLightAt(offsetPx: Offset, size: Offset): FloatingLight? {
        val hitRadius = size.minDimension * TranquilLightSessionConfig.LIGHT_HIT_RADIUS_FRACTION

        val hitLight = _uiState.value.lights
            .filter { it.state == LightMotionState.Floating }
            .minByOrNull { light ->
                val pos = TranquilLightSessionConfig.effectiveOffset(light)
                val px = Offset(pos.x * size.x, pos.y * size.y)
                hypot((px.x - offsetPx.x).toDouble(), (px.y - offsetPx.y).toDouble()).toFloat()
            }
            ?: return null

        val hitPos = TranquilLightSessionConfig.effectiveOffset(hitLight)
        val hitPx = Offset(hitPos.x * size.x, hitPos.y * size.y)
        return if (
            hypot(
                (hitPx.x - offsetPx.x).toDouble(),
                (hitPx.y - offsetPx.y).toDouble()
            ) <= hitRadius
        ) {
            hitLight
        } else {
            null
        }
    }

    private fun spawnBurst(origin: Offset, count: Int = TranquilLightSessionConfig.BURST_PARTICLE_COUNT) {
        val current = now()
        val newParticles = (0 until count).map {
            val angle = Random.nextFloat() * 6.28f
            val speed = 0.008f + Random.nextFloat() * 0.012f
            LightParticle(
                id = nextParticleId++,
                origin = origin,
                createdAtMs = current,
                lifespanMs = 550L + Random.nextLong(150L),
                kind = ParticleKind.Burst,
                velocity = Offset(
                    kotlin.math.cos(angle) * speed,
                    kotlin.math.sin(angle) * speed
                )
            )
        }
        addParticles(newParticles)
    }

    private fun spawnTrail(origin: Offset) {
        val particle = LightParticle(
            id = nextParticleId++,
            origin = origin,
            createdAtMs = now(),
            lifespanMs = 400L,
            kind = ParticleKind.Trail
        )
        addParticles(listOf(particle))
    }

    private fun spawnAmbientNearLantern() {
        val center = TranquilLightSessionConfig.LANTERN_CENTER
        val offset = Offset(
            center.x + (Random.nextFloat() - 0.5f) * 0.12f,
            center.y + (Random.nextFloat() - 0.5f) * 0.1f
        )
        val particle = LightParticle(
            id = nextParticleId++,
            origin = offset,
            createdAtMs = now(),
            lifespanMs = 2800L,
            kind = ParticleKind.Ambient
        )
        addParticles(listOf(particle))
    }

    private fun addParticles(newParticles: List<LightParticle>) {
        _uiState.update { state ->
            val merged = (state.particles + newParticles).takeLast(TranquilLightSessionConfig.MAX_PARTICLES)
            state.copy(particles = merged, clockMs = now())
        }
    }

    private fun triggerLanternPulse() {
        _uiState.update { it.copy(lanternPulse = 1f, clockMs = now()) }
    }

    private fun vitalityFor(collected: Int, total: Int): Float =
        (collected.toFloat() / total).coerceIn(0f, 1f)

    private fun startTravel(lightId: Int, fromOffset: Offset) {
        travelJob?.cancel()
        lastTrailSpawnMs = 0L
        _uiState.update { state ->
            state.copy(
                isTravelInProgress = true,
                lights = state.lights.map { light ->
                    if (light.id == lightId) {
                        light.copy(
                            state = LightMotionState.Traveling,
                            dragOffset = null,
                            travelStartOffset = fromOffset,
                            travelProgress = 0f
                        )
                    } else {
                        light
                    }
                },
                clockMs = now()
            )
        }

        travelJob = viewModelScope.launch {
            val steps = (TranquilLightSessionConfig.TRAVEL_DURATION_MS / 16L).toInt().coerceAtLeast(1)
            repeat(steps) { step ->
                if (!isActive) return@launch
                val progress = (step + 1).toFloat() / steps
                val currentMs = now()

                if (currentMs - lastTrailSpawnMs >= TranquilLightSessionConfig.TRAIL_SPAWN_INTERVAL_MS) {
                    val light = _uiState.value.lights.find { it.id == lightId }
                    if (light != null) {
                        val trailPos = TranquilLightSessionConfig.effectiveOffset(light)
                        spawnTrail(trailPos)
                        lastTrailSpawnMs = currentMs
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        lights = state.lights.map { light ->
                            if (light.id == lightId) light.copy(travelProgress = progress)
                            else light
                        },
                        clockMs = currentMs
                    )
                }
                delay(16L)
            }

            _uiState.update { state ->
                val newCollected = state.collectedCount + 1
                val vitality = vitalityFor(newCollected, state.totalLights)
                state.copy(
                    collectedCount = newCollected,
                    isTravelInProgress = false,
                    lights = state.lights.map { light ->
                        if (light.id == lightId) {
                            light.copy(
                                state = LightMotionState.Collected,
                                travelProgress = 1f,
                                travelStartOffset = null
                            )
                        } else {
                            light
                        }
                    },
                    lanternGlow = vitality,
                    sceneVitality = vitality,
                    clockMs = now()
                )
            }

            triggerLanternPulse()
            spawnBurst(TranquilLightSessionConfig.LANTERN_CENTER, count = 4)

            val collected = _uiState.value.collectedCount
            if (collected >= TranquilLightSessionConfig.TOTAL_LIGHTS) {
                completeSession()
            }
        }
    }

    private fun completeSession() {
        finishJob?.cancel()
        finishJob = viewModelScope.launch {
            val steps = (TranquilLightSessionConfig.LANTERN_GLOW_MS / 16L).toInt().coerceAtLeast(1)
            repeat(steps) { step ->
                if (!isActive) return@launch
                val glow = 0.7f + (step + 1).toFloat() / steps * 0.3f
                _uiState.update {
                    it.copy(
                        lanternGlow = glow.coerceIn(0f, 1f),
                        sceneVitality = glow.coerceIn(0f, 1f),
                        clockMs = now()
                    )
                }
                delay(16L)
            }
            floatJob?.cancel()
            particleJob?.cancel()
            ambientJob?.cancel()
            _uiState.update {
                it.copy(
                    screenPhase = TranquilLightScreenPhase.Finished,
                    lanternGlow = 1f,
                    sceneVitality = 1f,
                    clockMs = now()
                )
            }
        }
    }

    private fun startFloatTicker() {
        floatJob?.cancel()
        floatJob = viewModelScope.launch {
            while (isActive) {
                delay(TranquilLightSessionConfig.FLOAT_TICK_MS)
                _uiState.update { state ->
                    if (state.screenPhase != TranquilLightScreenPhase.Playing) return@update state
                    val driftSpeed = TranquilLightSessionConfig.DRIFT_SPEED
                    state.copy(
                        lights = state.lights.map { light ->
                            if (light.state == LightMotionState.Floating) {
                                light.copy(
                                    driftPhase = light.driftPhase + driftSpeed * light.driftMultiplier
                                )
                            } else {
                                light
                            }
                        },
                        clockMs = now()
                    )
                }
            }
        }
    }

    private fun startParticleTicker() {
        particleJob?.cancel()
        particleJob = viewModelScope.launch {
            while (isActive) {
                delay(TranquilLightSessionConfig.PARTICLE_TICK_MS)
                val current = now()
                _uiState.update { state ->
                    val alive = state.particles.filter { particle ->
                        particle.alphaAt(current) > 0f
                    }
                    if (alive.size == state.particles.size) state else state.copy(particles = alive, clockMs = current)
                }
            }
        }
    }

    private fun startAmbientTicker() {
        ambientJob?.cancel()
        ambientJob = viewModelScope.launch {
            while (isActive) {
                delay(TranquilLightSessionConfig.AMBIENT_TICK_MS)
                val state = _uiState.value
                if (state.screenPhase != TranquilLightScreenPhase.Playing) continue
                if (state.sceneVitality > 0.2f) {
                    spawnAmbientNearLantern()
                    if (state.sceneVitality > 0.5f) {
                        spawnAmbientNearLantern()
                    }
                }
            }
        }
    }

    private fun startPulseDecayTicker() {
        pulseDecayJob?.cancel()
        pulseDecayJob = viewModelScope.launch {
            while (isActive) {
                delay(16L)
                val state = _uiState.value
                if (state.lanternPulse <= 0f) continue
                val decayStep = 16f / TranquilLightSessionConfig.LANTERN_PULSE_DECAY_MS
                _uiState.update {
                    it.copy(
                        lanternPulse = (it.lanternPulse - decayStep).coerceAtLeast(0f),
                        clockMs = now()
                    )
                }
            }
        }
    }

    private fun cancelJobs() {
        floatJob?.cancel()
        particleJob?.cancel()
        ambientJob?.cancel()
        pulseDecayJob?.cancel()
        travelJob?.cancel()
        finishJob?.cancel()
        floatJob = null
        particleJob = null
        ambientJob = null
        pulseDecayJob = null
        travelJob = null
        finishJob = null
    }

    private fun now(): Long = System.currentTimeMillis()

    private val Offset.minDimension: Float
        get() = minOf(x, y)

    override fun onCleared() {
        cancelJobs()
        super.onCleared()
    }
}
