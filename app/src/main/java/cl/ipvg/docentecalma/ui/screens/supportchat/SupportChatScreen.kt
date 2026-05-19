package cl.ipvg.docentecalma.ui.screens.supportchat

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.domain.model.ChatMessage
import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun SupportChatScreen(
    onBack: () -> Unit,
    viewModel: SupportChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    DocenteCalmaScaffold(
        title = "Chat de apoyo",
        onBack = onBack,
        actions = {
            TextButton(onClick = { viewModel.onEvent(SupportChatEvent.OnClearSession) }) {
                Text("Nueva conversación")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isInitializing -> CenteredLoading(PaddingValues(0.dp))
                    state.showEmpty -> EmptyState()
                    else -> MessagesList(
                        messages = state.messages,
                        listState = listState
                    )
                }

                if (state.isGenerating) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Mascot(
                                state = MascotState.Thinking,
                                contentDescription = null,
                                sizeDp = 36.dp
                            )
                            Spacer(Modifier.widthIn(min = 6.dp))
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.height(14.dp)
                            )
                            Spacer(Modifier.widthIn(min = 8.dp))
                            Text(
                                MascotPersona.phraseFor(MascotState.Thinking)
                                    ?: "Preparando respuesta…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (state.lastReplyFromFallback) {
                FallbackNotice(
                    onDismiss = { viewModel.onEvent(SupportChatEvent.DismissFallbackNotice) }
                )
            }

            state.error?.let { err ->
                ErrorBar(
                    message = err.message,
                    hint = err.hint,
                    canRetry = state.canRetry,
                    onRetry = { viewModel.onEvent(SupportChatEvent.OnRetry) },
                    onDismiss = { viewModel.onEvent(SupportChatEvent.DismissError) }
                )
            }

            InputBar(
                value = state.input,
                isSending = state.isGenerating,
                canSend = state.canSend,
                onValueChange = { viewModel.onEvent(SupportChatEvent.OnInputChanged(it)) },
                onSend = { viewModel.onEvent(SupportChatEvent.OnSend) }
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(
            items = messages,
            key = { _, m -> m.id }
        ) { index, message ->
            MessageBubble(
                message = message,
                precedingUserText = lastUserMessageBefore(messages, index)
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    precedingUserText: String?
) {
    val isUser = message.role == ChatRole.USER
    val bg =
        if (isUser) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondaryContainer
    val fg =
        if (isUser) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSecondaryContainer
    val shape =
        if (isUser) RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
        else RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Mascot(
                state = mascotStateForAssistantBubble(precedingUserText),
                contentDescription = "${MascotPersona.NAME}, respuesta del asistente",
                sizeDp = 52.dp
            )
            Spacer(Modifier.widthIn(min = 8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    isSending: Boolean,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje…") },
                maxLines = 4,
                enabled = !isSending,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(modifier = Modifier.widthIn(min = 8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar mensaje"
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 360.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Mascot(
                    state = MascotState.Listening,
                    contentDescription = "Mascota ${MascotPersona.NAME} a la escucha",
                    sizeDp = 120.dp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Apoyo breve para tu día docente",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = MascotPersona.phraseFor(MascotState.Listening)
                        ?: "Cuando quieras, te leo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Puedo ayudarte a ordenar ideas, micro-pausas y estrategias " +
                        "sencillas de aula o autocuidado. No diagnostico ni doy tratamiento; " +
                        "si algo te supera o hay riesgo, conviene un canal humano o institucional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Este chat no sustituye terapia ni atención de salud mental; " +
                        "es una herramienta preventiva dentro de la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
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
private fun FallbackNotice(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mascot(
            state = MascotState.OfflineSad,
            contentDescription = null,
            sizeDp = 28.dp
        )
        Spacer(Modifier.widthIn(min = 8.dp))
        Text(
            text = MascotPersona.phraseFor(MascotState.OfflineSad)
                ?: "No pudimos usar el asistente en línea; esta respuesta viene del modo local de la app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onDismiss) { Text("Ok") }
    }
}

@Composable
private fun ErrorBar(
    message: String,
    hint: String?,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Mascot(
            state = MascotState.ErrorState,
            contentDescription = null,
            sizeDp = 28.dp
        )
        Spacer(Modifier.widthIn(min = 8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (!hint.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.92f)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (canRetry) {
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}
