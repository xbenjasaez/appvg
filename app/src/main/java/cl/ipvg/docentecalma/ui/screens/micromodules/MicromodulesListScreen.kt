package cl.ipvg.docentecalma.ui.screens.micromodules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.ui.components.ContinueReadingCard
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.components.FriendlySectionHeader
import cl.ipvg.docentecalma.ui.components.MicromoduleReadingCopy
import cl.ipvg.docentecalma.ui.components.ReadingHeroCard
import cl.ipvg.docentecalma.ui.components.ReadingModuleCard

@Composable
fun MicromodulesListScreen(
    onBack: () -> Unit,
    onOpenModule: (String) -> Unit,
    viewModel: MicromodulesListViewModel = hiltViewModel()
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val continueRow = continueRowFor(rows)
    val listRows = continueRow?.let { c -> rows.filter { it.module.id != c.module.id } } ?: rows

    DocenteCalmaScaffold(
        title = MicromoduleReadingCopy.listTopBarTitle,
        onBack = onBack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ReadingHeroCard(
                    title = MicromoduleReadingCopy.listHeroTitle,
                    subtitle = MicromoduleReadingCopy.listHeroSubtitle
                )
            }
            continueRow?.let { row ->
                item {
                    ContinueReadingCard(
                        module = row.module,
                        state = row.state,
                        onOpen = { onOpenModule(row.module.id) }
                    )
                }
            }
            item {
                FriendlySectionHeader(
                    title = MicromoduleReadingCopy.sectionAllTitle,
                    subtitle = MicromoduleReadingCopy.sectionAllSubtitle
                )
            }
            items(
                items = listRows,
                key = { it.module.id }
            ) { row ->
                ReadingModuleCard(
                    module = row.module,
                    state = row.state,
                    onClick = { onOpenModule(row.module.id) }
                )
            }
        }
    }
}

private fun continueRowFor(rows: List<MicromoduleListRow>): MicromoduleListRow? {
    rows.firstOrNull { it.state == MicromoduleProgressState.IN_PROGRESS }?.let { return it }
    rows.firstOrNull { it.state == MicromoduleProgressState.NOT_STARTED }?.let { return it }
    return null
}
