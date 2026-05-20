package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranquilLightScreen(
    onBack: () -> Unit,
    viewModel: TranquilLightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val handleIntroBack: () -> Unit = {
        viewModel.onEvent(TranquilLightEvent.OnExit)
        onBack()
    }

    val handlePlayingBack: () -> Unit = {
        viewModel.onEvent(TranquilLightEvent.OnExit)
        onBack()
    }

    val handleFinishBack: () -> Unit = {
        viewModel.onEvent(TranquilLightEvent.OnFinishGoBack)
        onBack()
    }

    val scaffoldTitle = when (state.screenPhase) {
        TranquilLightScreenPhase.Intro,
        TranquilLightScreenPhase.Finished -> TranquilLightCopy.title
        TranquilLightScreenPhase.Playing -> ""
    }

    DocenteCalmaScaffold(
        title = scaffoldTitle,
        onBack = when (state.screenPhase) {
            TranquilLightScreenPhase.Intro -> handleIntroBack
            TranquilLightScreenPhase.Playing -> null
            TranquilLightScreenPhase.Finished -> handleFinishBack
        },
        actions = {
            if (state.screenPhase == TranquilLightScreenPhase.Playing) {
                TextButton(onClick = handlePlayingBack) {
                    Text(TranquilLightCopy.ctaBack)
                }
            }
        }
    ) { padding ->
        when (state.screenPhase) {
            TranquilLightScreenPhase.Intro -> IntroContent(
                padding = padding,
                onStart = { viewModel.onEvent(TranquilLightEvent.OnStart) },
                onBack = handleIntroBack
            )
            TranquilLightScreenPhase.Playing -> PlayingContent(
                padding = padding,
                state = state,
                onDragStart = { viewModel.onEvent(TranquilLightEvent.OnDragStart(it)) },
                onDrag = { viewModel.onEvent(TranquilLightEvent.OnDrag(it)) },
                onDragEnd = { viewModel.onEvent(TranquilLightEvent.OnDragEnd) },
                onSizeChanged = { viewModel.onEvent(TranquilLightEvent.OnPlayfieldSizeChanged(it)) }
            )
            TranquilLightScreenPhase.Finished -> FinishedContent(
                padding = padding,
                onRepeat = { viewModel.onEvent(TranquilLightEvent.OnRepeat) },
                onFinishBack = handleFinishBack
            )
        }
    }
}

@Composable
private fun IntroContent(
    padding: PaddingValues,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TranquilLightIntroHero(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Mascot(
            state = MascotState.Greeting,
            contentDescription = "Mascota ${MascotPersona.NAME}, lista para acompañarte",
            sizeDp = 96.dp,
            animate = true
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = TranquilLightCopy.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = TranquilLightCopy.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TranquilLightCopy.introHelp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TranquilLightCopy.ctaStart)
        }

        TextButton(onClick = onBack) {
            Text(TranquilLightCopy.ctaBack)
        }
    }
}

@Composable
private fun PlayingContent(
    padding: PaddingValues,
    state: TranquilLightUiState,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onSizeChanged: (Offset) -> Unit
) {
    val vitality = state.sceneVitality.coerceIn(0f, 1f)
    val bottomMascotState = when {
        state.collectedCount >= state.totalLights / 2 -> MascotState.EmotionCalm
        state.isTravelInProgress -> MascotState.EmotionCalm
        state.isDragging -> MascotState.EmotionCalm
        else -> MascotState.Listening
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .semantics {
                contentDescription = TranquilLightCopy.playfieldContentDescription(
                    state.collectedCount,
                    state.totalLights
                )
            }
    ) {
        TranquilLightPlayfield(
            lights = state.lights,
            particles = state.particles,
            lanternGlow = state.lanternGlow,
            sceneVitality = state.sceneVitality,
            lanternPulse = state.lanternPulse,
            clockMs = state.clockMs,
            isInputEnabled = state.isInputEnabled || state.isDragging,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            onSizeChanged = onSizeChanged,
            modifier = Modifier.fillMaxSize()
        )

        TranquilLightPlayingHud(
            progressLabel = state.progressLabel,
            collectedCount = state.collectedCount,
            totalLights = state.totalLights,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        Mascot(
            state = bottomMascotState,
            contentDescription = "Mascota ${MascotPersona.NAME} acompañando tu pausa visual",
            sizeDp = 72.dp,
            animate = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .scale(
                    scaleX = -(1f + vitality * 0.04f),
                    scaleY = 1f + vitality * 0.04f
                )
        )
    }
}

@Composable
private fun FinishedContent(
    padding: PaddingValues,
    onRepeat: () -> Unit,
    onFinishBack: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "lanternFinishedPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lanternPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = TranquilLightSessionConfig.nightBackgroundDeep
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val lanternCenter = Offset(size.width * 0.5f, size.height * 0.55f)
                    val clockMs = System.currentTimeMillis()
                    drawNightGardenBackground(
                        vitality = 1f,
                        lanternCenter = lanternCenter,
                        clockMs = clockMs
                    )
                    drawLanternCore(
                        center = lanternCenter,
                        glow = pulse.coerceIn(0.9f, 1.1f),
                        clockMs = clockMs
                    )
                }
            }
        }

        Mascot(
            state = MascotState.Cheering,
            contentDescription = "Mascota ${MascotPersona.NAME} celebrando tu pausa",
            sizeDp = 96.dp,
            animate = true
        )

        Text(
            text = TranquilLightCopy.closingMain,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = TranquilLightCopy.closingSecondary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRepeat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TranquilLightCopy.ctaRepeat)
        }

        Button(
            onClick = onFinishBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TranquilLightCopy.ctaFinishBack)
        }
    }
}
