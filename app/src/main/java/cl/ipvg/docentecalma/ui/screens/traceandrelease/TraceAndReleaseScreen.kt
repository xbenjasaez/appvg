package cl.ipvg.docentecalma.ui.screens.traceandrelease

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.ipvg.docentecalma.ui.components.DocenteCalmaScaffold
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState
import cl.ipvg.docentecalma.ui.theme.IpvgBlueVirginio
import cl.ipvg.docentecalma.ui.theme.IpvgGrayBrand
import cl.ipvg.docentecalma.ui.theme.IpvgGreenSoft
import cl.ipvg.docentecalma.ui.theme.SecondaryContainerLight
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceAndReleaseScreen(
    onBack: () -> Unit,
    viewModel: TraceAndReleaseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val handleIntroBack: () -> Unit = {
        viewModel.onEvent(TraceAndReleaseEvent.OnExit)
        onBack()
    }

    val handleFinishBack: () -> Unit = {
        viewModel.onEvent(TraceAndReleaseEvent.OnFinishGoBack)
        onBack()
    }

    val scaffoldTitle = when (state.screenPhase) {
        TraceScreenPhase.Intro,
        TraceScreenPhase.Finished -> TraceAndReleaseCopy.title
        TraceScreenPhase.Playing -> ""
    }

    DocenteCalmaScaffold(
        title = scaffoldTitle,
        onBack = when (state.screenPhase) {
            TraceScreenPhase.Intro -> handleIntroBack
            TraceScreenPhase.Playing -> null
            TraceScreenPhase.Finished -> handleFinishBack
        },
        actions = {
            when (state.screenPhase) {
                TraceScreenPhase.Playing -> {
                    TextButton(
                        onClick = { viewModel.onEvent(TraceAndReleaseEvent.OnClear) },
                        enabled = state.interactionMode == TraceInteractionMode.Drawing
                    ) {
                        Text(TraceAndReleaseCopy.ctaClear)
                    }
                    TextButton(onClick = { viewModel.onEvent(TraceAndReleaseEvent.OnRequestClose) }) {
                        Text(TraceAndReleaseCopy.ctaBack)
                    }
                }
                else -> Unit
            }
        }
    ) { padding ->
        when (state.screenPhase) {
            TraceScreenPhase.Intro -> IntroContent(
                padding = padding,
                onStart = { viewModel.onEvent(TraceAndReleaseEvent.OnStart) },
                onBack = handleIntroBack
            )
            TraceScreenPhase.Playing -> PlayingContent(
                padding = padding,
                state = state,
                onDragStart = { viewModel.onEvent(TraceAndReleaseEvent.OnDragStart(it)) },
                onDrag = { viewModel.onEvent(TraceAndReleaseEvent.OnDrag(it)) },
                onDragEnd = { viewModel.onEvent(TraceAndReleaseEvent.OnDragEnd) }
            )
            TraceScreenPhase.Finished -> FinishedContent(
                padding = padding,
                onContinue = { viewModel.onEvent(TraceAndReleaseEvent.OnContinuePlaying) },
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
        TraceIntroHero(
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
                    text = TraceAndReleaseCopy.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = TraceAndReleaseCopy.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TraceAndReleaseCopy.introHelp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TraceAndReleaseCopy.ctaStart)
        }

        TextButton(onClick = onBack) {
            Text(TraceAndReleaseCopy.ctaBack)
        }
    }
}

@Composable
private fun TraceIntroHero(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    SecondaryContainerLight.copy(alpha = 0.55f),
                    Color(0xFFEAF4F8),
                    Color(0xFFF5F0E8),
                    IpvgGreenSoft.copy(alpha = 0.25f)
                )
            )
        )

        val waveBack = Path().apply {
            moveTo(0f, size.height * 0.68f)
            cubicTo(
                size.width * 0.2f, size.height * 0.52f,
                size.width * 0.5f, size.height * 0.82f,
                size.width, size.height * 0.64f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = waveBack,
            color = IpvgGrayBrand.copy(alpha = 0.08f)
        )

        val waveFront = Path().apply {
            moveTo(0f, size.height * 0.62f)
            cubicTo(
                size.width * 0.25f, size.height * 0.48f,
                size.width * 0.55f, size.height * 0.78f,
                size.width, size.height * 0.58f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = waveFront,
            color = IpvgBlueVirginio.copy(alpha = 0.12f)
        )

        val pathTrail = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.55f)
            quadraticBezierTo(
                size.width * 0.28f, size.height * 0.32f,
                size.width * 0.48f, size.height * 0.46f
            )
            quadraticBezierTo(
                size.width * 0.68f, size.height * 0.58f,
                size.width * 0.92f, size.height * 0.38f
            )
        }
        drawPath(
            path = pathTrail,
            color = IpvgBlueVirginio.copy(alpha = 0.22f),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        drawCircle(
            color = IpvgBlueVirginio.copy(alpha = 0.1f),
            radius = size.minDimension * 0.14f,
            center = Offset(size.width * 0.78f, size.height * 0.28f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = size.minDimension * 0.08f,
            center = Offset(size.width * 0.24f, size.height * 0.32f)
        )
        drawCircle(
            color = IpvgGreenSoft.copy(alpha = 0.35f),
            radius = size.minDimension * 0.05f,
            center = Offset(size.width * 0.52f, size.height * 0.42f)
        )
    }
}

@Composable
private fun PlayingContent(
    padding: PaddingValues,
    state: TraceAndReleaseUiState,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current
    val virgiWalk = state.virgiWalk

    val bottomMascotState = when {
        state.clearAcknowledged -> MascotState.Cheering
        virgiWalk != null -> MascotState.EmotionCalm
        else -> MascotState.Listening
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        TraceCanvas(
            strokes = state.strokes,
            activeStroke = state.activeStroke,
            particles = state.touchParticles,
            pathSprouts = state.pathSprouts,
            clockMs = state.clockMs,
            isInputEnabled = state.isInputEnabled,
            onDragStart = onDragStart,
            onDrag = onDrag,
            onDragEnd = onDragEnd,
            modifier = Modifier.fillMaxSize()
        )

        virgiWalk?.let { walk ->
            val mascotSizePx = with(density) { 52.dp.toPx() }
            val xPx = walk.position.x - mascotSizePx / 2f
            val yPx = walk.position.y - mascotSizePx / 2f
            val scaleX = if (walk.facingRight) 1f else -1f

            Mascot(
                state = MascotState.EmotionCalm,
                contentDescription = "Virgi recorre tu camino",
                sizeDp = 52.dp,
                animate = true,
                modifier = Modifier
                    .offset {
                        IntOffset(xPx.roundToInt(), yPx.roundToInt())
                    }
                    .scale(scaleX = scaleX, scaleY = 1f)
            )
        }

        if (state.guidedProgressLabel != null) {
            TracePlayingHud(
                guidedProgressLabel = state.guidedProgressLabel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = TraceAndReleaseCopy.canvasHint,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (virgiWalk == null) {
                Mascot(
                    state = bottomMascotState,
                    contentDescription = "Mascota ${MascotPersona.NAME} acompañando tu pausa visual",
                    sizeDp = 64.dp,
                    animate = true,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FinishedContent(
    padding: PaddingValues,
    onContinue: () -> Unit,
    onFinishBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Mascot(
            state = MascotState.EmotionHappy,
            contentDescription = "Mascota ${MascotPersona.NAME} celebrando tu pausa",
            sizeDp = 96.dp,
            animate = true
        )

        Text(
            text = TraceAndReleaseCopy.closingMain,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = TraceAndReleaseCopy.closingSecondary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TraceAndReleaseCopy.ctaContinue)
        }

        Button(
            onClick = onFinishBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(TraceAndReleaseCopy.ctaFinishBack)
        }
    }
}
