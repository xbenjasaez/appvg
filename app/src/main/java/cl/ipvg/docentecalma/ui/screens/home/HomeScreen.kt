package cl.ipvg.docentecalma.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.ui.components.BrandingHeader
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.components.HomeEntryCard
import cl.ipvg.docentecalma.ui.components.HomeEntryTier
import cl.ipvg.docentecalma.ui.components.HomeLatestCheckInCard
import cl.ipvg.docentecalma.ui.components.HomeSectionHeader
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgOrange
import cl.ipvg.docentecalma.ui.theme.IpvgYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navActions: HomeNavActions,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedbackSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.onHomeAppear()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.onHomeHidden() }
    }

    if (state.feedbackSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onFeedbackDismiss() },
            sheetState = feedbackSheetState
        ) {
            PostUseFeedbackSheetContent(
                onSubmit = { s, u, e, c -> viewModel.onFeedbackSubmit(s, u, e, c) },
                onNotNow = { viewModel.onFeedbackDismiss() }
            )
        }
    }

    DocenteCalmaScaffold(title = "Docente Calma") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            BrandingHeader(
                title = "Hola, docente",
                subtitle = "Espacio del IPVG para registrar cómo te encuentras, acceder al apoyo y fortalecer tu bienestar en el aula y la institución."
            )

            when {
                state.isLoading -> LoadingCard()
                state.error != null -> ErrorCard(
                    message = state.error!!,
                    onDismiss = { viewModel.onEvent(HomeEvent.DismissError) }
                )
                state.hasLatest -> HomeLatestCheckInCard(checkIn = state.latestCheckIn!!)
                state.showEmpty -> EmptyLatestCard(onStart = navActions.onNavigateToCheckIn)
            }

            HomeSectionHeader(
                title = "Empieza por aquí",
                subtitle = "Tres accesos principales para el día a día."
            )
            HomeEntryCard(
                icon = Icons.Filled.Favorite,
                title = "Registrar cómo me siento",
                description = "Chequeo breve con emoción, intensidad y una nota opcional.",
                tier = HomeEntryTier.PrimaryPortal,
                accentOverride = IpvgBlueVirginio,
                onClick = navActions.onNavigateToCheckIn
            )
            HomeEntryCard(
                icon = Icons.Filled.Assignment,
                title = "Autoevaluación breve",
                description = "Cuatro preguntas sobre tu semana, con retroalimentación en la app.",
                tier = HomeEntryTier.PrimaryPortal,
                accentOverride = IpvgBlueVirginio,
                onClick = navActions.onNavigateToSelfAssessment
            )
            HomeEntryCard(
                icon = Icons.Filled.ChatBubbleOutline,
                title = "Chat de apoyo",
                description = "Conversación socioemocional con acompañamiento orientativo (IA).",
                tier = HomeEntryTier.PrimaryPortal,
                accentOverride = IpvgOrange,
                onClick = navActions.onNavigateToSupportChat
            )

            HomeSectionHeader(
                title = "Recursos",
                subtitle = "Material de consulta y prácticas breves.",
                showDivider = true
            )
            HomeEntryCard(
                icon = Icons.Filled.School,
                title = "Guía de aula",
                description = "Orientación para situaciones complejas con estudiantes.",
                tier = HomeEntryTier.Resource,
                onClick = navActions.onNavigateToClassroomGuidance
            )
            HomeEntryCard(
                icon = Icons.Filled.Bolt,
                title = "Ejercicios breves",
                description = "Respiración, pausa activa y reencuadre en pocos minutos.",
                tier = HomeEntryTier.Resource,
                accentOverride = IpvgYellow,
                onClick = navActions.onNavigateToQuickExercises
            )
            HomeEntryCard(
                icon = Icons.Filled.MenuBook,
                title = "Lecturas",
                description = "Textos breves para acompañarte; tu avance queda solo en el teléfono.",
                tier = HomeEntryTier.Resource,
                onClick = navActions.onNavigateToMicromodules
            )

            HomeSectionHeader(
                title = "Tu evolución",
                subtitle = "Historial, progreso y datos personales.",
                showDivider = true
            )
            HomeEntryCard(
                icon = Icons.Filled.Timeline,
                title = "Historial",
                description = "Chequeos y recomendaciones registrados.",
                tier = HomeEntryTier.Utility,
                onClick = navActions.onNavigateToHistory
            )
            HomeEntryCard(
                icon = Icons.Filled.Insights,
                title = "Tu progreso",
                description = "Continuidad en la app, actividad reciente y autoevaluaciones.",
                tier = HomeEntryTier.Utility,
                onClick = navActions.onNavigateToProgress
            )
            HomeEntryCard(
                icon = Icons.Filled.PrivacyTip,
                title = "Privacidad",
                description = "Datos locales, consentimiento y borrado de historial.",
                tier = HomeEntryTier.Utility,
                onClick = navActions.onNavigateToPrivacy
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EmptyLatestCard(onStart: () -> Unit) {
    val shape = MaterialTheme.shapes.extraLarge
    ElevatedCard(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Mascot(
                    state = MascotState.Idle,
                    contentDescription = null,
                    sizeDp = 84.dp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aún no tienes un chequeo registrado",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "En menos de un minuto puedes dejar constancia de cómo te encuentras. Toca para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = MascotPersona.SHORT_BIO,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Cargando tu último chequeo…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Entendido")
            }
        }
    }
}
