package cl.ipvg.docentecalma.ui.screens.micromodules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.analytics.PilotFlowDurationBuckets
import cl.ipvg.docentecalma.data.analytics.PilotFlowSecondaryKeys
import cl.ipvg.docentecalma.data.repository.MicromoduleProgressRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.domain.model.Micromodule
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog
import cl.ipvg.docentecalma.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class MicromoduleReaderUiState(
    val module: Micromodule?,
    val progress: MicromoduleProgressState,
    val error: String? = null
) {
    val isValid: Boolean get() = module != null
}

sealed interface MicromoduleReaderEvent {
    data object OnMarkCompleted : MicromoduleReaderEvent
    data object DismissError : MicromoduleReaderEvent
}

@HiltViewModel
class MicromoduleReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressRepository: MicromoduleProgressRepository,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    private val moduleId: String =
        savedStateHandle.get<String>(Routes.ARG_MODULE_ID).orEmpty()

    private val module: Micromodule? = MicromoduleCatalog.byIdOrNull(moduleId)

    private val flowStartedAtMs: Long? =
        if (module != null) System.currentTimeMillis() else null

    private val errorFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MicromoduleReaderUiState> = combine(
        progressRepository.observeAll(),
        errorFlow
    ) { progressList, err ->
        val progress = progressList.find { it.moduleId == moduleId }?.state
            ?: MicromoduleProgressState.NOT_STARTED
        MicromoduleReaderUiState(
            module = module,
            progress = if (module == null) MicromoduleProgressState.NOT_STARTED else progress,
            error = err
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MicromoduleReaderUiState(
            module = module,
            progress = MicromoduleProgressState.NOT_STARTED,
            error = errorFlow.value
        )
    )

    init {
        if (module != null) {
            viewModelScope.launch {
                runCatching { progressRepository.markOpened(moduleId) }
                    .onSuccess { firstEverOpen ->
                        if (firstEverOpen) {
                            pilotAnalyticsRepository.record(
                                PilotEventNames.MODULE_STARTED,
                                secondaryKey = moduleId
                            )
                        }
                    }
                    .onFailure { t ->
                        errorFlow.value = t.message ?: "No se pudo guardar el progreso."
                    }
            }
        }
    }

    fun onEvent(event: MicromoduleReaderEvent) {
        when (event) {
            MicromoduleReaderEvent.DismissError -> errorFlow.update { null }
            MicromoduleReaderEvent.OnMarkCompleted -> markCompleted()
        }
    }

    private fun markCompleted() {
        if (module == null) return
        viewModelScope.launch {
            errorFlow.update { null }
            runCatching { progressRepository.markCompleted(moduleId) }
                .onSuccess {
                    pilotAnalyticsRepository.record(
                        PilotEventNames.MODULE_COMPLETED,
                        secondaryKey = moduleId
                    )
                }
                .onFailure { t ->
                    errorFlow.value = t.message ?: "No se pudo marcar como visto."
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val start = flowStartedAtMs ?: return
        val seconds = ((System.currentTimeMillis() - start) / 1000).toInt().coerceAtLeast(0)
        val bucket = PilotFlowDurationBuckets.bucket(seconds)
        runBlocking {
            pilotAnalyticsRepository.record(
                PilotEventNames.FLOW_DURATION_BUCKET,
                secondaryKey = PilotFlowSecondaryKeys.micromodule(moduleId),
                intMeta = bucket
            )
        }
    }
}
