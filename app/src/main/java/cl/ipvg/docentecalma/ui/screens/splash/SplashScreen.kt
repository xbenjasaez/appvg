package cl.ipvg.docentecalma.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.ipvg.docentecalma.R
import cl.ipvg.docentecalma.ui.mascot.Mascot
import cl.ipvg.docentecalma.ui.mascot.MascotPersona
import cl.ipvg.docentecalma.ui.mascot.MascotState

/**
 * Pantalla de carga institucional. Muestra el logo del IP Virginio Gómez,
 * el nombre del producto y una barra fina de avance, antes de pasar al Home
 * o al onboarding inicial según el consentimiento guardado.
 *
 * - Duración corta (≈1.4s) para no entorpecer el inicio.
 * - Animación suave de aparición del logo.
 * - Branding centralizado: el splash es el primer toque visual con la marca.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var startAnim by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "splashAlpha"
    )

    val isPreview = LocalInspectionMode.current
    LocalContext.current

    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        startAnim = true
    }

    LaunchedEffect(destination, isPreview) {
        if (isPreview) return@LaunchedEffect
        when (destination) {
            is SplashViewModel.SplashDestination.Home -> {
                viewModel.consumeDestination()
                onNavigateToHome()
            }
            is SplashViewModel.SplashDestination.Onboarding -> {
                viewModel.consumeDestination()
                onNavigateToOnboarding()
            }
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Mascot(
                state = MascotState.Greeting,
                contentDescription = "Mascota ${MascotPersona.NAME} saludando",
                sizeDp = 180.dp,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_ipvg),
                contentDescription = "Logo IP Virginio Gómez",
                modifier = Modifier
                    .widthIn(max = 200.dp)
                    .alpha(alpha),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Docente Calma",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bienestar socioemocional para docentes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(36.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .widthIn(max = 180.dp)
                    .alpha(alpha),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
