package cl.ipvg.docentecalma.ui.screens.classroomguidance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.model.ClassroomScenario
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotEmptyState
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun ClassroomGuidanceScreen(
    onBack: () -> Unit,
    viewModel: ClassroomGuidanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DocenteCalmaScaffold(title = "Guía de aula", onBack = onBack) { padding ->
        when {
            state.isLoading -> CenteredLoading(padding)
            state.error != null -> CenteredMessage(
                padding = padding,
                message = state.error!!,
                isError = true
            )
            state.showEmpty -> CenteredMessage(
                padding = padding,
                message = "Aún no hay escenarios disponibles.",
                isError = false
            )
            else -> ScenariosList(
                padding = padding,
                scenarios = state.scenarios,
                onScenarioClick = { id ->
                    viewModel.onEvent(ClassroomGuidanceEvent.OnScenarioSelected(id))
                }
            )
        }

        state.selectedScenario?.let { scenario ->
            ScenarioDialog(
                scenario = scenario,
                onDismiss = { viewModel.onEvent(ClassroomGuidanceEvent.OnCloseDetail) }
            )
        }
    }
}

@Composable
private fun ScenariosList(
    padding: PaddingValues,
    scenarios: List<ClassroomScenario>,
    onScenarioClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (scenarios.isNotEmpty()) {
            item(key = "intro-listening") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Mascot(
                        state = MascotState.Listening,
                        contentDescription = "${MascotPersona.NAME}, guía de aula",
                        sizeDp = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Elige un escenario para ver pasos concretos y cuándo escalar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        items(items = scenarios, key = { it.id }) { scenario ->
            ElevatedCard(
                onClick = { onScenarioClick(scenario.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = scenario.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scenario.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioDialog(
    scenario: ClassroomScenario,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = { Text(scenario.title) },
        text = {
            Column {
                Text(
                    text = "Pasos",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                scenario.steps.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }

                if (scenario.redFlags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Banderas rojas",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    scenario.redFlags.forEach {
                        Text("• $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cuándo escalar",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = scenario.whenToEscalate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Composable
private fun CenteredLoading(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun CenteredMessage(padding: PaddingValues, message: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        MascotEmptyState(
            state = if (isError) MascotState.ErrorState else MascotState.Listening,
            message = message,
            isError = isError,
            mascotSize = 68.dp,
            contentDescription = if (isError) {
                "${MascotPersona.NAME}, error en guía de aula"
            } else {
                "${MascotPersona.NAME}, guía de aula vacía"
            }
        )
    }
}
