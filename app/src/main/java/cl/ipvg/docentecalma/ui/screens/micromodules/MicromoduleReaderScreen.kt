package cl.ipvg.docentecalma.ui.screens.micromodules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.model.Micromodule
import cl.ipvg.docentecalma.domain.model.MicromoduleBlock
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.components.GentleNoteCard
import cl.ipvg.docentecalma.ui.components.MicromoduleReadingCopy
import cl.ipvg.docentecalma.ui.components.ReadingActionFooter
import cl.ipvg.docentecalma.ui.components.ReadingDetailHero
import cl.ipvg.docentecalma.ui.components.StepBlockCard
import cl.ipvg.docentecalma.ui.components.isGentleNoteBlock
import cl.ipvg.docentecalma.ui.mascot.MascotEmptyState
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun MicromoduleReaderScreen(
    onBack: () -> Unit,
    onOpenExercises: () -> Unit,
    viewModel: MicromoduleReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val msg = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onEvent(MicromoduleReaderEvent.DismissError)
    }

    DocenteCalmaScaffold(
        title = MicromoduleReadingCopy.readerTopBarTitle,
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            !state.isValid -> EmptyModule(padding)
            else -> ReaderContent(
                padding = padding,
                module = state.module!!,
                progress = state.progress,
                onMarkCompleted = { viewModel.onEvent(MicromoduleReaderEvent.OnMarkCompleted) },
                onOpenExercises = onOpenExercises
            )
        }
    }
}

@Composable
private fun EmptyModule(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        MascotEmptyState(
            state = MascotState.ErrorState,
            message = "No encontramos esta lectura.",
            isError = true,
            mascotSize = 72.dp,
            contentDescription = "${MascotPersona.NAME}, lectura no disponible"
        )
    }
}

@Composable
private fun ReaderContent(
    padding: PaddingValues,
    module: Micromodule,
    progress: MicromoduleProgressState,
    onMarkCompleted: () -> Unit,
    onOpenExercises: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ReadingDetailHero(module = module, progress = progress)

        Text(
            text = "Lo esencial",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        module.blocks.forEach { block ->
            ReadingBlock(block = block)
        }

        ReadingActionFooter(
            showExerciseButton = module.relatedExerciseId != null,
            onOpenExercises = onOpenExercises,
            progress = progress,
            onMarkCompleted = onMarkCompleted,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun ReadingBlock(block: MicromoduleBlock) {
    when {
        isGentleNoteBlock(block.heading) -> GentleNoteCard(block = block)
        else -> StepBlockCard(block = block)
    }
}
