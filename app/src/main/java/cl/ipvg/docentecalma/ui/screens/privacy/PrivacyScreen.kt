package cl.ipvg.docentecalma.ui.screens.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onReviewConsent: () -> Unit,
    onOpenPilotMetrics: (() -> Unit)? = null,
    viewModel: PrivacyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isClearing by viewModel.isClearingHistory.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        val msg = userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeUserMessage()
    }

    DocenteCalmaScaffold(
        title = "Privacidad y datos",
        onBack = onBack,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Qué guarda la app en tu dispositivo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mascot(
                    state = MascotState.Idle,
                    contentDescription = null,
                    sizeDp = 44.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${MascotPersona.SHORT_BIO} Aquí ves el resumen; " +
                        "nada sale de tu teléfono salvo lo que indica la sección de IA.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            DataInventoryCard(
                checkInCount = uiState.counts.emotionalCheckIns,
                recommendationCount = uiState.counts.recommendations,
                chatMessageCount = uiState.counts.chatMessages,
                selfAssessmentCount = uiState.counts.selfAssessments,
                micromoduleProgressCount = uiState.counts.micromoduleProgressRows,
                hasPostUseFeedback = uiState.counts.hasPostUseFeedback
            )

            PrivacySection(
                title = "Chequeos emocionales",
                body = "Estado anotado, intensidad y nota opcional. Sirven para tu " +
                    "historial y progreso personal. Sensibles: pueden describir tu ánimo " +
                    "o situaciones del aula."
            )
            PrivacySection(
                title = "Autoevaluaciones breves",
                body = "Respuestas a cuatro preguntas de autoinforme (última semana), " +
                    "fecha y tipo de registro. Sirven para comparar contigo mismo/a en el " +
                    "tiempo; no son un instrumento clínico."
            )
            PrivacySection(
                title = "Recomendaciones vistas",
                body = "Registro de lo que la app te sugirió tras un chequeo. Operativo " +
                    "para no repetir el mismo consejo sin contexto."
            )
            PrivacySection(
                title = "Chat de apoyo",
                body = "Mensajes que escribiste y respuestas guardadas en el dispositivo. " +
                    "Sensibles: pueden contener detalles personales. Al usar el chat, el " +
                    "texto se envía a la API de Gemini para generar la respuesta."
            )
            PrivacySection(
                title = "Micromódulos",
                body = "Estado de lectura (no iniciado, en progreso o completado) y fechas " +
                    "asociadas en este dispositivo. El texto de los micromódulos no se guarda: " +
                    "solo tu avance."
            )
            PrivacySection(
                title = "Preferencias",
                body = "Si aceptaste el aviso inicial, queda guardado en archivos internos " +
                    "de la app (no en la nube de Docente Calma). Si respondiste el feedback breve " +
                    "tras varios usos, la puntuación y un comentario opcional quedan solo aquí. " +
                    "También hay un identificador técnico opaco por instalación para eventos de " +
                    "seguridad en memoria; no es tu nombre ni tu correo."
            )

            ConsentCard(
                consentAccepted = uiState.consentAccepted,
                onReviewConsent = { viewModel.resetConsentForReview(onReviewConsent) }
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Borrar historial local",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elimina chequeos, autoevaluaciones breves, recomendaciones guardadas, " +
                            "progreso de micromódulos, conversaciones del chat y el feedback breve local " +
                            "(puntuación y comentario) en este dispositivo. " +
                            "No borra tu aceptación del aviso ni el identificador técnico de instalación. " +
                            "No borra lo que ya pudo procesar el proveedor de IA.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        enabled = !isClearing && uiState.counts.hasAnyStoredHistory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Borrar historial…")
                    }
                    if (isClearing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            if (onOpenPilotMetrics != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.privacy_pilot_card_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.privacy_pilot_card_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onOpenPilotMetrics,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.privacy_pilot_card_action))
                        }
                    }
                }
            }

            PrivacySection(
                title = "Uso de la IA",
                body = "Cuando usas el chat de apoyo, tus mensajes se envían a la API de " +
                    "Gemini para generar una respuesta. Tus chequeos y notas no se envían nunca."
            )
            PrivacySection(
                title = "Detección de riesgo",
                body = "Si en el chat aparecen señales de riesgo (autolesión, daño a " +
                    "otra persona, violencia o crisis aguda), la app prioriza " +
                    "respuestas de seguridad y deriva a apoyo profesional. En esta versión, " +
                    "los eventos de riesgo no se guardan en disco; solo una cola breve en memoria " +
                    "que también se vacía al borrar historial. En la app publicada no se escriben " +
                    "registros técnicos de esos eventos en el sistema; en compilaciones de " +
                    "desarrollo puede haber trazas mínimas para depuración."
            )
            PrivacySection(
                title = "Apoyo institucional",
                body = "La app no reemplaza la atención profesional. Si lo necesitas, " +
                    "conversa con tu jefe/a de carrera, acude a registro académico del " +
                    "IP Virginio Gómez (Instituto Profesional Virginio Gómez) o utiliza " +
                    "el botón de denuncias disponible en la intranet."
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isClearing) showClearConfirm = false },
            title = { Text(text = "¿Borrar historial en este dispositivo?") },
            text = {
                Text(
                    text = "Se eliminarán tus chequeos, autoevaluaciones breves, recomendaciones " +
                        "registradas, mensajes del chat guardados aquí y cualquier feedback breve " +
                        "local (puntuación y comentario). Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearLocalHistoryAfterConfirmation()
                    },
                    enabled = !isClearing
                ) {
                    Text(text = "Borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirm = false },
                    enabled = !isClearing
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DataInventoryCard(
    checkInCount: Int,
    recommendationCount: Int,
    chatMessageCount: Int,
    selfAssessmentCount: Int,
    micromoduleProgressCount: Int,
    hasPostUseFeedback: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen local",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            InventoryRow(label = "Chequeos emocionales", count = checkInCount)
            InventoryRow(label = "Autoevaluaciones breves", count = selfAssessmentCount)
            InventoryRow(label = "Recomendaciones", count = recommendationCount)
            InventoryRow(label = "Micromódulos (registros de avance)", count = micromoduleProgressCount)
            InventoryRow(label = "Mensajes de chat guardados", count = chatMessageCount)
            InventoryRow(
                label = "Feedback breve (puntuación o comentario local)",
                count = if (hasPostUseFeedback) 1 else 0
            )
        }
    }
}

@Composable
private fun InventoryRow(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConsentCard(
    consentAccepted: Boolean,
    onReviewConsent: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consentimiento",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (consentAccepted) {
                    "Registramos que aceptaste el aviso inicial (uso voluntario y datos en el dispositivo)."
                } else {
                    "Aún no consta la aceptación del aviso en este dispositivo."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onReviewConsent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Revisar aviso y consentimiento")
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
