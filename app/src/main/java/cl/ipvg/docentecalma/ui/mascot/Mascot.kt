package cl.ipvg.docentecalma.ui.mascot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Cara del personaje Docente Calma. Resuelve el [MascotState] al recurso
 * adecuado y le aplica una animación sutil:
 *
 * - Si [MascotResources.lottieFor] devuelve un raw, se renderiza el Lottie
 *   en loop infinito (animación profesional).
 * - Si no hay Lottie, se carga el drawable estático y se le aplica la
 *   animación por código de [MascotMotionFactory.forState] (escala leve,
 *   rotación, rebote vertical) para que la ilustración no se sienta
 *   congelada.
 *
 * En modo preview (Compose @Preview) la animación se desactiva para que
 * el render quede estable.
 *
 * @param state estado visual a representar.
 * @param modifier modifier base del contenedor (sin el tamaño).
 * @param contentDescription texto para lectores de pantalla. Pásalo `null`
 *  cuando la mascota sea decorativa y exista otro texto cercano que ya
 *  describa el contexto.
 * @param sizeDp tamaño cuadrado renderizado.
 * @param animate `true` (default) aplica la animación por código a los
 *  estados estáticos. Pásalo `false` para escenarios donde el movimiento
 *  estorba (capturas, snapshots de test).
 */
@Composable
internal fun Mascot(
    state: MascotState,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    sizeDp: Dp = 96.dp,
    animate: Boolean = true
) {
    val isPreview = LocalInspectionMode.current
    val lottieRes = MascotResources.lottieFor(state)

    if (lottieRes != null) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
        if (composition != null) {
            val a11yModifier = if (contentDescription != null) {
                modifier.semantics { this.contentDescription = contentDescription }
            } else {
                modifier
            }
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = a11yModifier.size(sizeDp)
            )
            return
        }
    }

    val motion = if (animate && !isPreview) {
        MascotMotionFactory.forState(state)
    } else {
        MascotMotion.None
    }

    // `rememberInfiniteTransition` se llama siempre (slot-table-safe). Cuando
    // `motion` es inactivo, las amplitudes son 0 y el resultado neutraliza
    // el phase: la imagen queda quieta sin coste extra.
    val transition = rememberInfiniteTransition(label = "mascotMotion")
    val phase by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = motion.periodMs,
                easing = motion.easing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotPhase"
    )

    val sizePx = with(LocalDensity.current) { sizeDp.toPx() }
    val translationY = phase * motion.translateYFraction * sizePx
    val scale = 1f + phase * motion.scaleAmplitude
    val rotationDeg = phase * motion.rotationAmplitudeDeg

    Image(
        painter = painterResource(id = MascotResources.drawableFor(state)),
        contentDescription = contentDescription,
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationY = translationY,
                rotationZ = rotationDeg
            ),
        contentScale = ContentScale.Fit
    )
}
