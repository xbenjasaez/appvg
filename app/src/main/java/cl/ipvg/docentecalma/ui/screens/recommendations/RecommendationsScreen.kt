package cl.ipvg.docentecalma.ui.screens.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import cl.ipvg.docentecalma.domain.mapper.EmotionLabels
import cl.ipvg.docentecalma.domain.mapper.displayName
import cl.ipvg.docentecalma.domain.model.Recommendation
import cl.ipvg.docentecalma.domain.model.SeverityFlag
import cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.MascotEmptyState
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun RecommendationsScreen(
    onBack: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenMicromodule: (String) -> Unit,
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DocenteCalmaScaffold(title = "Recomendación", onBack = onBack) { padding ->
        when {
            state.isLoading -> LoadingState(padding)
            state.error != null -> RecommendationsErrorPanel(
                padding = padding,
                message = state.error!!,
                onRetry = { viewModel.onEvent(RecommendationsEvent.OnRetry) },
                onBack = onBack
            )
            state.hasData -> ContentState(
                padding = padding,
                recommendation = state.recommendation!!,
                acknowledged = state.acknowledged,
                onAcknowledge = { viewModel.onEvent(RecommendationsEvent.OnAcknowledge) },
                onOpenExercises = onOpenExercises,
                onOpenChat = onOpenChat,
                onOpenMicromodule = onOpenMicromodule
            )
            else -> RecommendationsEmptyPanel(padding = padding, onBack = onBack)
        }
    }
}

@Composable
private fun ContentState(
    padding: PaddingValues,
    recommendation: Recommendation,
    acknowledged: Boolean,
    onAcknowledge: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenMicromodule: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderCard(recommendation = recommendation)

        SectionCard(title = "Acción inmediata") {
            Text(
                text = recommendation.immediateAction,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SectionCard(title = "Respiración sugerida") {
            Text(
                text = recommendation.breathingSuggestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SectionCard(title = "Qué evitar") {
            Text(
                text = recommendation.whatToAvoid,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        recommendation.optionalPedagogicalTip?.let { tip ->
            SectionCard(title = "Tip pedagógico") {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        recommendation.suggestedExercise?.let { exercise ->
            SectionCard(title = "Ejercicio disponible") {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${exercise.durationMinutes} min · ${exercise.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        recommendation.suggestedMicromoduleId?.let { moduleId ->
            val mm = MicromoduleCatalog.byIdOrNull(moduleId)
            if (mm != null) {
                SectionCard(title = "Lectura que puede ayudarte") {
                    Text(
                        text = mm.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${mm.estimatedMinutes} min · ${mm.lead}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onOpenMicromodule(moduleId) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Abrir esta lectura") }
                }
            }
        }

        SeverityCta(
            severity = recommendation.severity,
            onOpenChat = onOpenChat
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (recommendation.suggestedExercise != null) {
            OutlinedButton(
                onClick = onOpenExercises,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ir a ejercicios breves") }
        }

        Button(
            onClick = onAcknowledge,
            enabled = !acknowledged,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (acknowledged) "Recomendación aplicada" else "Marcar como aplicada")
        }
    }
}

@Composable
private fun HeaderCard(recommendation: Recommendation) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${recommendation.emotion.displayName} · ${
                    EmotionLabels.intensityLabel(recommendation.intensity)
                } (${recommendation.intensity}/5)",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.shortMessage,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
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
private fun SeverityCta(
    severity: SeverityFlag,
    onOpenChat: () -> Unit
) {
    when (severity) {
        SeverityFlag.NORMAL -> Unit
        SeverityFlag.SUGGEST_CHAT -> ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Si lo necesitas, puedes abrir el chat de apoyo para conversar.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenChat) { Text("Abrir chat de apoyo") }
            }
        }
        SeverityFlag.SUGGEST_PROFESSIONAL -> ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "La intensidad es alta. Considera contactar a registro académico, " +
                        "seguir los conductos regulares del instituto o a un profesional de salud mental.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenChat) { Text("Abrir chat de apoyo") }
            }
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun RecommendationsErrorPanel(
    padding: PaddingValues,
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MascotEmptyState(
            state = MascotState.ErrorState,
            message = message,
            isError = true,
            mascotSize = 68.dp,
            contentDescription = "${MascotPersona.NAME}, error al cargar recomendación"
        )
        TextButton(onClick = onRetry) { Text("Reintentar") }
        TextButton(onClick = onBack) { Text("Volver al inicio") }
    }
}

@Composable
private fun RecommendationsEmptyPanel(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MascotEmptyState(
            state = MascotState.Listening,
            message = "No encontramos una recomendación para este chequeo. " +
                "Puede ser un problema temporal; vuelve al inicio e inténtalo de nuevo.",
            isError = false,
            mascotSize = 68.dp,
            contentDescription = "${MascotPersona.NAME}, sin recomendación"
        )
        TextButton(onClick = onBack) { Text("Volver al inicio") }
    }
}
