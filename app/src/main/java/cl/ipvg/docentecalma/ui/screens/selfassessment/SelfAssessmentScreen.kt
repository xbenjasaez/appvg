package cl.ipvg.docentecalma.ui.screens.selfassessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentNavHint
import cl.ipvg.docentecalma.domain.rules.SelfAssessmentQuestionnaire
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold

data class SelfAssessmentNavActions(
    val onOpenCheckIn: () -> Unit,
    val onOpenExercises: () -> Unit,
    val onOpenClassroom: () -> Unit,
    val onOpenChat: () -> Unit
)

@Composable
fun SelfAssessmentScreen(
    onBack: () -> Unit,
    navActions: SelfAssessmentNavActions,
    viewModel: SelfAssessmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DocenteCalmaScaffold(title = "Autoevaluación breve", onBack = onBack) { padding ->
        when (state.phase) {
            SelfAssessmentPhase.INTRO -> IntroContent(
                padding = padding,
                history = state.historyPreview,
                onStart = { viewModel.onEvent(SelfAssessmentEvent.Start) }
            )
            SelfAssessmentPhase.QUESTIONS -> QuestionsContent(
                padding = padding,
                questionIndex = state.questionIndex,
                selected = state.answers.getOrNull(state.questionIndex),
                isSaving = state.isSaving,
                error = state.error,
                onSelect = { viewModel.onEvent(SelfAssessmentEvent.OnAnswerSelected(it)) },
                onNext = { viewModel.onEvent(SelfAssessmentEvent.OnNext) },
                onBackQuestion = { viewModel.onEvent(SelfAssessmentEvent.OnQuestionBack) },
                onDismissError = { viewModel.onEvent(SelfAssessmentEvent.DismissError) }
            )
            SelfAssessmentPhase.RESULT -> {
                val r = state.result
                if (r != null) {
                    ResultContent(
                        padding = padding,
                        result = r,
                        navActions = navActions,
                        onNew = { viewModel.onEvent(SelfAssessmentEvent.OnNewAssessment) }
                    )
                } else {
                    IntroContent(
                        padding = padding,
                        history = state.historyPreview,
                        onStart = { viewModel.onEvent(SelfAssessmentEvent.Start) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroContent(
    padding: PaddingValues,
    history: List<SelfAssessmentHistoryRowUi>,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cuatro preguntas sobre la última semana. Es un autoinforme de apoyo: " +
                "no evalúa tu desempeño ni reemplaza una evaluación profesional de salud.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Puedes repetirla cuando quieras; el historial se guarda solo en tu dispositivo.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar")
        }
        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registros recientes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            history.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = row.dateLabel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${row.totalScore}/${SelfAssessment.MAX_TOTAL} · ${row.typeLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionsContent(
    padding: PaddingValues,
    questionIndex: Int,
    selected: Int?,
    isSaving: Boolean,
    error: String?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onBackQuestion: () -> Unit,
    onDismissError: () -> Unit
) {
    val progress = (questionIndex + 1).toFloat() / SelfAssessment.QUESTION_COUNT.toFloat()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Pregunta ${questionIndex + 1} de ${SelfAssessment.QUESTION_COUNT}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = SelfAssessmentQuestionnaire.prompts[questionIndex],
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = SelfAssessmentQuestionnaire.SCALE_LOW_LABEL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = SelfAssessmentQuestionnaire.SCALE_HIGH_LABEL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnswerScaleRow(selected = selected, onSelect = onSelect, enabled = !isSaving)
        error?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onDismissError) { Text("Cerrar aviso") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackQuestion, enabled = !isSaving) {
                Text("Atrás")
            }
            Button(
                onClick = onNext,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        if (questionIndex == SelfAssessment.QUESTION_COUNT - 1) {
                            "Ver resultado"
                        } else {
                            "Siguiente"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerScaleRow(
    selected: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (v in SelfAssessment.SCORE_RANGE) {
            FilterChip(
                selected = selected == v,
                onClick = { onSelect(v) },
                label = { Text(v.toString()) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun ResultContent(
    padding: PaddingValues,
    result: SelfAssessmentResultUi,
    navActions: SelfAssessmentNavActions,
    onNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tu resultado (autoinforme)",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${result.totalScore} de ${result.maxTotal} puntos",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.typeLabel,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.recordedAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(text = result.summary, style = MaterialTheme.typography.bodyMedium)
        result.comparison?.let { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = "Sugerencias en la app",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        result.suggestions.forEach { s ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = s.title, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navActions.navigate(s.navHint) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir")
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onNew,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva autoevaluación")
        }
    }
}

private fun SelfAssessmentNavActions.navigate(hint: SelfAssessmentNavHint) {
    when (hint) {
        SelfAssessmentNavHint.EMOTIONAL_CHECK_IN -> onOpenCheckIn()
        SelfAssessmentNavHint.QUICK_EXERCISES -> onOpenExercises()
        SelfAssessmentNavHint.CLASSROOM_GUIDANCE -> onOpenClassroom()
        SelfAssessmentNavHint.SUPPORT_CHAT -> onOpenChat()
    }
}
