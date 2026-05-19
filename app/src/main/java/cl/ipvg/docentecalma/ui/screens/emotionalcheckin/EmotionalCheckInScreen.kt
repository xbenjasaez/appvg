package cl.ipvg.docentecalma.ui.screens.emotionalcheckin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.mapper.EmotionLabels
import cl.ipvg.docentecalma.domain.mapper.displayName
import cl.ipvg.docentecalma.domain.mapper.label
import cl.ipvg.docentecalma.domain.mapper.shortDescription
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotEmotionMapper
import cl.ipvg.docentecalma.ui.mascot.MascotPersona

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmotionalCheckInScreen(
    onBack: () -> Unit,
    onNavigateToRecommendations: (Long) -> Unit,
    viewModel: EmotionalCheckInViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is EmotionalCheckInEffect.Saved -> onNavigateToRecommendations(effect.checkInId)
            }
        }
    }

    DocenteCalmaScaffold(
        title = "¿Cómo te sientes?",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmotionalMirror(
                selected = state.selectedEmotion
            )

            Text(
                text = "Selecciona tu emoción",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.emotions.forEach { emotion ->
                    EmotionChip(
                        emotion = emotion,
                        selected = state.selectedEmotion == emotion,
                        onClick = {
                            viewModel.onEvent(EmotionalCheckInEvent.OnEmotionSelected(emotion))
                        }
                    )
                }
            }

            state.selectedEmotion?.let { selected ->
                Text(
                    text = selected.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Categoría: ${selected.category.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Intensidad: ${EmotionLabels.intensityLabel(state.intensity)} (${state.intensity}/${state.intensityRange.last})",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.intensityRange.forEach { value ->
                    FilterChip(
                        selected = state.intensity == value,
                        onClick = {
                            viewModel.onEvent(EmotionalCheckInEvent.OnIntensityChanged(value))
                        },
                        label = { Text(value.toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Text(
                text = "Nota opcional",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = {
                    viewModel.onEvent(EmotionalCheckInEvent.OnNoteChanged(it))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Algo que quieras dejar registrado…") },
                supportingText = {
                    Text("${state.note.length}/${EmotionalCheckInUiState.NOTE_MAX_LENGTH}")
                },
                minLines = 3,
                maxLines = 5
            )

            state.error?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { viewModel.onEvent(EmotionalCheckInEvent.OnSave) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isSaving) {
                        "Guardando…"
                    } else {
                        "Guardar y ver qué te sugiere la app"
                    }
                )
            }
        }
    }
}

@Composable
private fun EmotionChip(
    emotion: Emotion,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(emotion.displayName) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Espejo emocional: la mascota refleja en vivo la emoción seleccionada.
 * Acompaña con una frase breve de [MascotPersona] coherente con el estado.
 *
 * Cuando todavía no hay emoción seleccionada, queda en `Idle` con la bio.
 */
@Composable
private fun EmotionalMirror(selected: Emotion?) {
    val state = MascotEmotionMapper.fromEmotion(selected)
    val phrase = MascotPersona.phraseFor(state) ?: MascotPersona.SHORT_BIO
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mascot(
            state = state,
            contentDescription = "Mascota ${MascotPersona.NAME}",
            sizeDp = 96.dp
        )
        Spacer(Modifier.widthIn(min = 12.dp))
        Text(
            text = phrase,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
