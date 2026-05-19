package cl.ipvg.docentecalma.ui.screens.progress

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.MascotEmptyState
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
import java.util.Locale

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onStartCheckIn: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DocenteCalmaScaffold(title = "Tu progreso", onBack = onBack) { padding ->
        when {
            state.isLoading -> CenteredLoading(padding)
            state.error != null -> CenteredMessage(
                padding = padding,
                message = state.error!!,
                isError = true,
                onDismiss = { viewModel.onEvent(ProgressEvent.DismissError) }
            )
            state.showEmpty -> CenteredMessage(
                padding = padding,
                message = "Cuando registres cómo te sientes, hagas una autoevaluación o guardes " +
                    "un paso desde las recomendaciones, aquí verás un resumen tranquilo de tu camino.",
                isError = false,
                onDismiss = null,
                primaryActionLabel = "Empezar con un chequeo",
                onPrimaryAction = onStartCheckIn
            )
            else -> ProgressContent(padding = padding, state = state)
        }
    }
}

@Composable
private fun ProgressContent(padding: PaddingValues, state: ProgressUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IntroCard()

        ContinuityCard(state = state)

        if (state.insightLines.isNotEmpty()) {
            SectionCard(title = "Algo que podría ayudarte a mirar tu historia") {
                state.insightLines.forEach { line ->
                    Text(
                        text = "· $line",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        if (state.timeline.isNotEmpty()) {
            SectionCard(title = "Lo más reciente") {
                Text(
                    text = "Ordenado por fecha. Son tus registros en el teléfono, sin enviarse a ningún servidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                state.timeline.forEach { row ->
                    TimelineRow(row = row)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        if (state.selfAssessmentRows.isNotEmpty()) {
            SectionCard(title = "Autoevaluaciones") {
                state.selfAssessmentRows.forEach { row ->
                    SelfAssessmentBlock(row = row)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        state.emotionalSummary?.let { summary ->
            SectionCard(title = "Tu registro emocional") {
                Text(
                    text = "Una lectura sencilla de tus chequeos, sin compararte con nadie más.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val avg = summary.averageIntensity?.let {
                    String.format(Locale("es", "CL"), "%.1f", it)
                } ?: "—"
                val freq = summary.mostFrequentLabel ?: "—"
                Text(
                    text = "Últimos 7 días: ${summary.checkInsLast7} registros · intensidad " +
                        "promedio $avg/5 · emoción más presente: $freq",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Últimos 30 días: ${summary.checkInsLast30} registros · " +
                        "en total en la app: ${summary.totalCheckIns}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Tus datos viven solo en este dispositivo. Esta pantalla es para ti, no para evaluarte.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IntroCard() {
    SectionCard(title = "Un espacio para mirar atrás con calma") {
        Text(
            text = "No es un informe ni una nota. Es una forma de ver cómo has ido usando la app " +
                "y qué ha pasado en tus últimas semanas.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ContinuityCard(state: ProgressUiState) {
    SectionCard(title = "Continuidad") {
        val n = state.activeDaysInContinuityWindow
        val w = state.continuityWindowDays
        Text(
            text = "En los últimos $w días hubo actividad en $n día(s) distinto(s): chequeos, " +
                "autoevaluaciones o pasos guardados.",
            style = MaterialTheme.typography.bodyMedium
        )
        state.lastActivityRelative?.let { rel ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Lo último que quedó registrado: $rel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.savedStepsCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            val extra =
                if (state.exerciseStepsCount > 0) {
                    " ${state.exerciseStepsCount} vinculado(s) a ejercicio o pausa."
                } else {
                    ""
                }
            Text(
                text = "Desde las recomendaciones guardaste ${state.savedStepsCount} paso(s).$extra " +
                    "(Si abriste un ejercicio solo desde el menú principal, puede no aparecer aquí.)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimelineRow(row: ProgressTimelineRow) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = row.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = row.relative,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = row.secondary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SelfAssessmentBlock(row: ProgressSelfAssessmentRow) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = row.title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = row.relative,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = row.scoreLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
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
private fun CenteredMessage(
    padding: PaddingValues,
    message: String,
    isError: Boolean,
    onDismiss: (() -> Unit)?,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MascotEmptyState(
                state = if (isError) MascotState.ErrorState else MascotState.Idle,
                message = message,
                isError = isError,
                mascotSize = 68.dp,
                contentDescription = if (isError) {
                    "${MascotPersona.NAME}, error en progreso"
                } else {
                    "${MascotPersona.NAME}, progreso sin datos"
                }
            )
            if (primaryActionLabel != null && onPrimaryAction != null) {
                Button(onClick = onPrimaryAction) { Text(primaryActionLabel) }
            }
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    }
}


