package cl.ipvg.docentecalma.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import cl.ipvg.docentecalma.domain.mapper.displayName
import cl.ipvg.docentecalma.domain.model.ChatSessionSummary
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.RecommendationType
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.MascotEmptyState
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
import cl.ipvg.docentecalma.util.DateTimeFormatters

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onStartCheckIn: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DocenteCalmaScaffold(title = "Historial", onBack = onBack) { padding ->
        when {
            state.isLoading -> CenteredLoading(padding)
            state.error != null -> CenteredMessage(
                padding = padding,
                message = state.error!!,
                isError = true,
                onDismiss = { viewModel.onEvent(HistoryEvent.DismissError) }
            )
            state.showEmpty -> CenteredMessage(
                padding = padding,
                message = "Todavía no hay nada aquí. Cuando registres cómo te sientes, verás " +
                    "tus chequeos y el resto de tu actividad en un solo lugar.",
                isError = false,
                onDismiss = null,
                primaryActionLabel = "Registrar cómo me siento",
                onPrimaryAction = onStartCheckIn
            )
            else -> HistoryList(
                padding = padding,
                state = state,
                onDelete = { id -> viewModel.onEvent(HistoryEvent.OnDeleteCheckIn(id)) }
            )
        }
    }
}

@Composable
private fun HistoryList(
    padding: PaddingValues,
    state: HistoryUiState,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.checkIns.isNotEmpty()) {
            item(key = "header-checkins") { SectionHeader("Chequeos emocionales") }
            items(items = state.checkIns, key = { "checkin-${it.id}" }) { item ->
                CheckInCard(item = item, onDelete = { onDelete(item.id) })
            }
        }

        if (state.recommendations.isNotEmpty()) {
            item(key = "header-recs") { SectionHeader("Recomendaciones registradas") }
            items(items = state.recommendations, key = { "rec-${it.id}" }) { rec ->
                RecommendationCard(item = rec)
            }
        }

        if (state.chatSessions.isNotEmpty()) {
            item(key = "header-chat") { SectionHeader("Conversaciones recientes") }
            items(items = state.chatSessions, key = { "chat-${it.sessionId}" }) { session ->
                ChatSessionCard(item = session)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CheckInCard(item: EmotionalCheckIn, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${item.emotion.displayName} · intensidad ${item.intensity}/5",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = DateTimeFormatters.full(item.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!item.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.note, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(onClick = onDelete) {
                Text("Eliminar")
            }
        }
    }
}

@Composable
private fun RecommendationCard(item: RecommendationHistory) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recommendationTitle(item),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = DateTimeFormatters.full(item.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${item.emotion.displayName} · intensidad ${item.intensity}/5",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatSessionCard(item: ChatSessionSummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.preview ?: "Conversación sin título",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.messageCount} mensajes · " +
                    DateTimeFormatters.relative(item.lastAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun recommendationTitle(item: RecommendationHistory): String = when (item.type) {
    RecommendationType.IMMEDIATE -> "Acción inmediata"
    RecommendationType.EXERCISE -> "Ejercicio breve"
    RecommendationType.CHAT -> "Apoyo en chat"
    RecommendationType.PROFESSIONAL -> "Derivación profesional"
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
                    "${MascotPersona.NAME}, error en historial"
                } else {
                    "${MascotPersona.NAME}, historial vacío"
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
