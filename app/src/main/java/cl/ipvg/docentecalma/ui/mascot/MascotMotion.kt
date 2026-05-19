package cl.ipvg.docentecalma.ui.mascot

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

/**
 * Parámetros de animación por código aplicados a una mascota estática.
 *
 * El componente [Mascot] usa estos valores para producir un movimiento
 * sutil sobre la ilustración estática (PNG) — respiración, oscilación,
 * temblor leve, rebote, etc. Es la alternativa "ligera" a Lottie:
 * permite dar vida al personaje sin necesidad de animaciones manuales.
 *
 * Convención:
 * - Las amplitudes se expresan como fracción positiva: el componente
 *   las hace oscilar de `-amp` a `+amp` con suavizado.
 * - `translateYFraction` se mide en fracción del lado renderizado en
 *   dp (ej. 0.04 sobre 100dp = ±4dp).
 * - `periodMs` es la duración de medio ciclo (ida). Con `RepeatMode.Reverse`
 *   el ciclo completo dura el doble.
 */
internal data class MascotMotion(
    val scaleAmplitude: Float = 0f,
    val rotationAmplitudeDeg: Float = 0f,
    val translateYFraction: Float = 0f,
    val periodMs: Int = 3000,
    val easing: Easing = FastOutSlowInEasing
) {
    /** `true` si al menos una propiedad anima algo perceptible. */
    val isActive: Boolean
        get() = scaleAmplitude != 0f ||
            rotationAmplitudeDeg != 0f ||
            translateYFraction != 0f

    companion object {
        /** Sin movimiento. Útil para preview o reduce-motion. */
        val None: MascotMotion = MascotMotion()
    }
}

/**
 * Tabla de movimientos por estado. El criterio:
 * - Estados de presencia (Idle, Listening): respiración suave.
 * - Estados activos (Greeting, Cheering): gesto amplio.
 * - Estados de espera (Thinking): rebote corto.
 * - Estados emocionales: el cuerpo se mueve coherente con la emoción
 *   (anxious vibra, sad casi quieto, happy bota, tired apenas oscila).
 * - Ejercicios: ritmo asociado al ejercicio (breathing lento, grounding
 *   gira, stretching rebota, etc.).
 */
internal object MascotMotionFactory {

    fun forState(state: MascotState): MascotMotion = when (state) {
        // Presencia — respiración suave
        MascotState.Idle,
        MascotState.Listening -> MascotMotion(
            scaleAmplitude = 0.02f,
            periodMs = 1800
        )

        // Saludo — leve oscilación lateral (cabeza/cuerpo)
        MascotState.Greeting -> MascotMotion(
            rotationAmplitudeDeg = 3f,
            periodMs = 900
        )

        // Pensando — rebote vertical corto (typing)
        MascotState.Thinking -> MascotMotion(
            translateYFraction = 0.04f,
            periodMs = 500
        )

        // Apoyo — rebote moderado
        MascotState.Cheering -> MascotMotion(
            translateYFraction = 0.05f,
            rotationAmplitudeDeg = 1.5f,
            periodMs = 450
        )

        // Empático — pulso muy lento, casi quieto
        MascotState.Empathic -> MascotMotion(
            scaleAmplitude = 0.015f,
            periodMs = 2400
        )

        // Sin conexión — oscilación lenta y resignada
        MascotState.OfflineSad -> MascotMotion(
            rotationAmplitudeDeg = 1.5f,
            periodMs = 1400
        )

        // Error — vibración corta de "atención"
        MascotState.ErrorState -> MascotMotion(
            rotationAmplitudeDeg = 2f,
            periodMs = 350
        )

        // Ejercicios
        MascotState.Breathing -> MascotMotion(
            // Inhalar/exhalar: ciclo lento.
            // Para 4-7-8 real se necesita keyframes; aquí una versión
            // simple: respiración de 8s ida-vuelta.
            scaleAmplitude = 0.05f,
            periodMs = 4000
        )
        MascotState.Grounding -> MascotMotion(
            rotationAmplitudeDeg = 5f,
            periodMs = 1300
        )
        MascotState.Stretching -> MascotMotion(
            translateYFraction = 0.04f,
            rotationAmplitudeDeg = 2f,
            periodMs = 800
        )
        MascotState.Reframing -> MascotMotion(
            scaleAmplitude = 0.025f,
            periodMs = 1800
        )
        MascotState.Resting -> MascotMotion(
            scaleAmplitude = 0.02f,
            periodMs = 3200
        )

        // Espejo emocional
        MascotState.EmotionCalm -> MascotMotion(
            scaleAmplitude = 0.025f,
            periodMs = 2600
        )
        MascotState.EmotionAnxious -> MascotMotion(
            rotationAmplitudeDeg = 0.8f,
            periodMs = 180,
            easing = LinearEasing
        )
        MascotState.EmotionFrustrated -> MascotMotion(
            rotationAmplitudeDeg = 1.2f,
            periodMs = 320
        )
        MascotState.EmotionSad -> MascotMotion(
            translateYFraction = 0.015f,
            periodMs = 3000
        )
        MascotState.EmotionHappy -> MascotMotion(
            translateYFraction = 0.05f,
            periodMs = 500
        )
        MascotState.EmotionTired -> MascotMotion(
            translateYFraction = 0.012f,
            periodMs = 3600
        )
    }
}
