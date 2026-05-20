package cl.ipvg.docentecalma.ui.screens.cloudandclarity

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
import kotlin.random.Random

@HiltViewModel
class CloudAndClarityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CloudAndClarityUiState())
    val uiState: StateFlow<CloudAndClarityUiState> = _uiState.asStateFlow()

    private var mask: RevealMask = RevealMask.fullFog(
        CloudAndClaritySessionConfig.MASK_COLS,
        CloudAndClaritySessionConfig.MASK_ROWS
    )
    private var lastNormalizedErase: Offset? = null
    private var ambientJob: Job? = null
    private var completeJob: Job? = null
    private var pulseDecayJob: Job? = null
    private var lastSceneVariant: CloudSceneVariant? = null

    fun onEvent(event: CloudAndClarityEvent) {
        when (event) {
            CloudAndClarityEvent.OnStart -> startPlaying()
            CloudAndClarityEvent.OnExit -> exitSession()
            CloudAndClarityEvent.OnRepeat -> startPlaying()
            CloudAndClarityEvent.OnFinishGoBack -> resetToIntro()
            is CloudAndClarityEvent.OnEraseStart -> handleEraseStart(event.offsetPx)
            is CloudAndClarityEvent.OnErase -> handleErase(event.offsetPx)
            CloudAndClarityEvent.OnEraseEnd -> handleEraseEnd()
            is CloudAndClarityEvent.OnPlayfieldSizeChanged -> {
                _uiState.update { it.copy(playfieldSize = event.sizePx) }
            }
        }
    }

    private fun startPlaying() {
        cancelJobs()
        lastNormalizedErase = null
        val variant = CloudSceneCatalog.nextVariant(lastSceneVariant)
        lastSceneVariant = variant
        mask = RevealMask.fullFog(
            CloudAndClaritySessionConfig.MASK_COLS,
            CloudAndClaritySessionConfig.MASK_ROWS
        )
        _uiState.value = CloudAndClarityUiState(
            screenPhase = CloudAndClarityScreenPhase.Playing,
            sceneVariant = variant,
            maskCells = mask.copyCells(),
            clearedPercent = 0,
            sceneVitality = 0f,
            clockMs = now(),
            mistSeed = Random.nextInt()
        )
        startAmbientTicker()
        startPulseDecayTicker()
    }

    private fun exitSession() {
        cancelJobs()
        lastNormalizedErase = null
        lastSceneVariant = null
        _uiState.value = CloudAndClarityUiState()
    }

    private fun resetToIntro() {
        cancelJobs()
        lastNormalizedErase = null
        mask = RevealMask.fullFog(
            CloudAndClaritySessionConfig.MASK_COLS,
            CloudAndClaritySessionConfig.MASK_ROWS
        )
        _uiState.value = CloudAndClarityUiState(screenPhase = CloudAndClarityScreenPhase.Intro)
    }

    private fun handleEraseStart(offsetPx: Offset) {
        if (!_uiState.value.isInputEnabled) return
        lastNormalizedErase = null
        applyErase(offsetPx, isStart = true)
        _uiState.update { it.copy(isErasing = true, clockMs = now()) }
    }

    private fun handleErase(offsetPx: Offset) {
        if (!_uiState.value.isInputEnabled && _uiState.value.screenPhase != CloudAndClarityScreenPhase.Playing) {
            return
        }
        applyErase(offsetPx, isStart = false)
    }

    private fun handleEraseEnd() {
        lastNormalizedErase = null
        _uiState.update { it.copy(isErasing = false, lastErasePx = null, clockMs = now()) }
    }

    private fun applyErase(offsetPx: Offset, isStart: Boolean) {
        val size = _uiState.value.playfieldSize
        if (size.x <= 0f || size.y <= 0f) return

        val normalized = Offset(
            (offsetPx.x / size.x).coerceIn(0f, 1f),
            (offsetPx.y / size.y).coerceIn(0f, 1f)
        )
        val brush = CloudAndClaritySessionConfig.BRUSH_RADIUS_NORM

        if (isStart || lastNormalizedErase == null) {
            mask.eraseNormalized(normalized, brush)
        } else {
            mask.eraseStroke(
                from = lastNormalizedErase!!,
                to = normalized,
                radiusNorm = brush
            )
        }
        lastNormalizedErase = normalized

        val vitality = mask.clearedFraction
        val percent = displayPercent(vitality)

        _uiState.update {
            it.copy(
                maskCells = mask.copyCells(),
                clearedPercent = percent,
                sceneVitality = vitality,
                lastErasePx = offsetPx,
                erasePulse = 1f,
                clockMs = now()
            )
        }

        if (vitality >= CloudAndClaritySessionConfig.COMPLETION_THRESHOLD) {
            scheduleCompletion()
        }
    }

    private fun scheduleCompletion() {
        if (completeJob?.isActive == true) return
        completeJob = viewModelScope.launch {
            delay(CloudAndClaritySessionConfig.COMPLETE_HOLD_MS)
            if (!isActive) return@launch
            ambientJob?.cancel()
            val finalVitality = mask.clearedFraction
            val finalPercent = displayPercent(finalVitality)
            _uiState.update { current ->
                current.copy(
                    screenPhase = CloudAndClarityScreenPhase.Finished,
                    sceneVitality = finalVitality.coerceIn(0f, 1f),
                    clearedPercent = finalPercent,
                    maskCells = mask.copyCells(),
                    clockMs = now(),
                    isErasing = false
                )
            }
        }
    }

    private fun displayPercent(clearedFraction: Float): Int {
        val raw = (clearedFraction * 100f).toInt().coerceIn(0, 100)
        return if (clearedFraction >= CloudAndClaritySessionConfig.COMPLETION_THRESHOLD) {
            100
        } else {
            raw
        }
    }

    private fun startAmbientTicker() {
        ambientJob?.cancel()
        ambientJob = viewModelScope.launch {
            while (isActive) {
                delay(CloudAndClaritySessionConfig.AMBIENT_TICK_MS)
                val state = _uiState.value
                if (state.screenPhase != CloudAndClarityScreenPhase.Playing) continue
                _uiState.update { it.copy(clockMs = now()) }
            }
        }
    }

    private fun startPulseDecayTicker() {
        pulseDecayJob?.cancel()
        pulseDecayJob = viewModelScope.launch {
            while (isActive) {
                delay(16L)
                val state = _uiState.value
                if (state.erasePulse <= 0f) continue
                _uiState.update {
                    it.copy(erasePulse = (it.erasePulse - 0.08f).coerceAtLeast(0f))
                }
            }
        }
    }

    private fun cancelJobs() {
        ambientJob?.cancel()
        completeJob?.cancel()
        pulseDecayJob?.cancel()
        ambientJob = null
        completeJob = null
        pulseDecayJob = null
    }

    private fun now(): Long = System.currentTimeMillis()

    override fun onCleared() {
        cancelJobs()
        super.onCleared()
    }
}
