package cl.ipvg.docentecalma.ui.screens.micromodules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.repository.MicromoduleProgressRepository
import cl.ipvg.docentecalma.domain.model.Micromodule
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MicromoduleListRow(
    val module: Micromodule,
    val state: MicromoduleProgressState
)

@HiltViewModel
class MicromodulesListViewModel @Inject constructor(
    progressRepository: MicromoduleProgressRepository
) : ViewModel() {

    val rows: StateFlow<List<MicromoduleListRow>> = progressRepository.observeAll()
        .map { progressList ->
            val byId = progressList.associate { it.moduleId to it.state }
            MicromoduleCatalog.all.map { m ->
                MicromoduleListRow(
                    module = m,
                    state = byId[m.id] ?: MicromoduleProgressState.NOT_STARTED
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MicromoduleCatalog.all.map { MicromoduleListRow(it, MicromoduleProgressState.NOT_STARTED) }
        )
}
