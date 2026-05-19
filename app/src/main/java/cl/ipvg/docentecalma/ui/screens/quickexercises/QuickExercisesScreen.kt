package cl.ipvg.docentecalma.ui.screens.quickexercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.domain.model.QuickExercise
import cl.ipvg.docentecalma.domain.rules.QuickExerciseCatalog
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotResources
import cl.ipvg.docentecalma.ui.mascot.MascotState

/**
 * Listado de ejercicios breves de regulación (respiración, grounding,
 * pausa activa, reencuadre, micro descanso). Fuente única: [QuickExerciseCatalog].
 */
@Composable
fun QuickExercisesScreen(onBack: () -> Unit) {
    DocenteCalmaScaffold(title = "Ejercicios breves", onBack = onBack) { padding ->
        QuickExercisesContent(
            padding = padding,
            exercises = QuickExerciseCatalog.all
        )
    }
}

@Composable
private fun QuickExercisesContent(
    padding: PaddingValues,
    exercises: List<QuickExercise>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header() }
        items(items = exercises, key = { it.id }) { exercise ->
            ExerciseCard(exercise = exercise)
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Mascot(
            state = MascotState.Stretching,
            contentDescription = "Mascota ${MascotPersona.NAME} invitando a la pausa",
            sizeDp = 80.dp
        )
        Spacer(Modifier.widthIn(min = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pausas y regulación",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Elige uno según lo que necesites ahora: respirar, bajar activación, " +
                    "descargar tensión o volver al presente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseCard(exercise: QuickExercise) {
    val mascotState = MascotResources.stateForExerciseId(exercise.id)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Mascot(
                    state = mascotState,
                    contentDescription = null,
                    sizeDp = 64.dp
                )
                Spacer(Modifier.widthIn(min = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${exercise.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            exercise.steps.forEachIndexed { index, step ->
                StepRow(index = index + 1, text = step)
                if (index != exercise.steps.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StepRow(index: Int, text: String) {
    Text(
        text = "$index. $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}
