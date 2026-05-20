package cl.ipvg.docentecalma.ui.screens.cloudandclarity

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
fun CloudAndClarityScreen(
    onBack: () -> Unit,
    viewModel: CloudAndClarityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val handleIntroBack: () -> Unit = {
        viewModel.onEvent(CloudAndClarityEvent.OnExit)
        onBack()
    }

    val handlePlayingBack: () -> Unit = {
        viewModel.onEvent(CloudAndClarityEvent.OnExit)
        onBack()
    }

    val handleFinishBack: () -> Unit = {
        viewModel.onEvent(CloudAndClarityEvent.OnFinishGoBack)
        onBack()
    }

    val scaffoldTitle = when (state.screenPhase) {
        CloudAndClarityScreenPhase.Intro,
        CloudAndClarityScreenPhase.Finished -> CloudAndClarityCopy.title
        CloudAndClarityScreenPhase.Playing -> ""
    }

    DocenteCalmaScaffold(
        title = scaffoldTitle,
        onBack = when (state.screenPhase) {
            CloudAndClarityScreenPhase.Intro -> handleIntroBack
            CloudAndClarityScreenPhase.Playing -> null
            CloudAndClarityScreenPhase.Finished -> handleFinishBack
        },
        actions = {
            if (state.screenPhase == CloudAndClarityScreenPhase.Playing) {
                TextButton(onClick = handlePlayingBack) {
                    Text(CloudAndClarityCopy.ctaBack)
                }
            }
        }
    ) { padding ->
        when (state.screenPhase) {
            CloudAndClarityScreenPhase.Intro -> IntroContent(
                padding = padding,
                onStart = { viewModel.onEvent(CloudAndClarityEvent.OnStart) },
                onBack = handleIntroBack
            )
            CloudAndClarityScreenPhase.Playing -> PlayingContent(
                padding = padding,
                state = state,
                onEraseStart = { viewModel.onEvent(CloudAndClarityEvent.OnEraseStart(it)) },
                onErase = { viewModel.onEvent(CloudAndClarityEvent.OnErase(it)) },
                onEraseEnd = { viewModel.onEvent(CloudAndClarityEvent.OnEraseEnd) },
                onSizeChanged = { viewModel.onEvent(CloudAndClarityEvent.OnPlayfieldSizeChanged(it)) }
            )
            CloudAndClarityScreenPhase.Finished -> FinishedContent(
                padding = padding,
                sceneVariant = state.sceneVariant,
                onRepeat = { viewModel.onEvent(CloudAndClarityEvent.OnRepeat) },
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
        CloudAndClarityIntroHero(
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
                    text = CloudAndClarityCopy.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CloudAndClarityCopy.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CloudAndClarityCopy.introHelp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(CloudAndClarityCopy.ctaStart)
        }

        TextButton(onClick = onBack) {
            Text(CloudAndClarityCopy.ctaBack)
        }
    }
}

@Composable
private fun PlayingContent(
    padding: PaddingValues,
    state: CloudAndClarityUiState,
    onEraseStart: (androidx.compose.ui.geometry.Offset) -> Unit,
    onErase: (androidx.compose.ui.geometry.Offset) -> Unit,
    onEraseEnd: () -> Unit,
    onSizeChanged: (androidx.compose.ui.geometry.Offset) -> Unit
) {
    val vitality = state.sceneVitality.coerceIn(0f, 1f)
    val mascotState = when {
        state.clearedPercent >= 40 -> MascotState.EmotionCalm
        state.isErasing -> MascotState.Listening
        else -> MascotState.Listening
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .semantics {
                contentDescription = CloudAndClarityCopy.playfieldContentDescription(state.clearedPercent)
            }
    ) {
        CloudRevealPlayfield(
            sceneVariant = state.sceneVariant,
            maskCells = state.maskCells,
            sceneVitality = state.sceneVitality,
            clockMs = state.clockMs,
            mistSeed = state.mistSeed,
            erasePulse = state.erasePulse,
            lastErasePx = state.lastErasePx,
            isInputEnabled = state.isInputEnabled,
            onEraseStart = onEraseStart,
            onErase = onErase,
            onEraseEnd = onEraseEnd,
            onSizeChanged = onSizeChanged,
            modifier = Modifier.fillMaxSize()
        )

        CloudAndClarityPlayingHud(
            sceneVariant = state.sceneVariant,
            progressLabel = state.progressLabel,
            clearedPercent = state.clearedPercent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        Mascot(
            state = mascotState,
            contentDescription = "Mascota ${MascotPersona.NAME} acompañando tu pausa visual",
            sizeDp = 72.dp,
            animate = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(0.55f + vitality * 0.45f)
                .scale(0.92f + vitality * 0.08f)
        )
    }
}

@Composable
private fun FinishedContent(
    padding: PaddingValues,
    sceneVariant: CloudSceneVariant,
    onRepeat: () -> Unit,
    onFinishBack: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "cloudFinishedPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudFinishedGlow"
    )
    val colors = CloudSceneCatalog.colors(sceneVariant)
    val layout = CloudSceneCatalog.layout(sceneVariant)

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
            color = colors.skyBottom
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCalmSceneBackground(
                        vitality = 1f,
                        clockMs = System.currentTimeMillis(),
                        colors = colors,
                        layout = layout
                    )
                    drawCalmSceneGlow(
                        vitality = pulse.coerceIn(0.9f, 1.1f),
                        clockMs = System.currentTimeMillis(),
                        colors = colors
                    )
                }
            }
        }

        Mascot(
            state = MascotState.EmotionHappy,
            contentDescription = "Mascota ${MascotPersona.NAME} celebrando tu claridad",
            sizeDp = 96.dp,
            animate = true
        )

        Text(
            text = CloudAndClarityCopy.closingMain,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = CloudAndClarityCopy.closingSecondary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRepeat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(CloudAndClarityCopy.ctaRepeat)
        }

        Button(
            onClick = onFinishBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(CloudAndClarityCopy.ctaFinishBack)
        }
    }
}
