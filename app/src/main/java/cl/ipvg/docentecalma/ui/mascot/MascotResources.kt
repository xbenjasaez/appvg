package cl.ipvg.docentecalma.ui.mascot

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import cl.ipvg.docentecalma.R

/**
 * Tabla central de recursos del personaje. Es la ÚNICA fuente que mapea un
 * [MascotState] a un drawable o a un Lottie.
 *
 * Estrategia de placeholder:
 * - Mientras el equipo gráfico del IPVG entrega las variantes, todos los
 *   estados estáticos resuelven al asset base [R.drawable.mascot_base].
 * - Para los estados animados (ejercicios), [lottieFor] devuelve `null`
 *   hasta que el archivo `.json` esté disponible en `res/raw/`.
 * - El Composable [Mascot] hace fallback al drawable si no hay Lottie.
 *
 * Para incorporar nuevas variantes: editar este archivo (no los call sites).
 */
internal object MascotResources {

    /**
     * Drawable estático asociado al estado. Es la fuente garantizada — todo
     * estado tiene drawable, incluso los animados (lo usamos como fallback
     * mientras el Lottie no llegue o no logre cargar).
     */
    @DrawableRes
    fun drawableFor(state: MascotState): Int = when (state) {
        // Branding
        MascotState.Greeting -> R.drawable.mascot_greeting
        MascotState.Idle -> R.drawable.mascot_idle
        MascotState.Cheering -> R.drawable.mascot_cheering

        // Chat / IA
        MascotState.Listening -> R.drawable.mascot_listening
        MascotState.Thinking -> R.drawable.mascot_thinking
        MascotState.Empathic -> R.drawable.mascot_empathic
        MascotState.OfflineSad -> R.drawable.mascot_offline_sad
        MascotState.ErrorState -> R.drawable.mascot_error

        // Ejercicios breves
        MascotState.Breathing -> R.drawable.mascot_base
        MascotState.Grounding -> R.drawable.mascot_base
        MascotState.Stretching -> R.drawable.mascot_base
        MascotState.Reframing -> R.drawable.mascot_base
        MascotState.Resting -> R.drawable.mascot_base

        // Espejo emocional
        MascotState.EmotionCalm -> R.drawable.mascot_emotion_calm
        MascotState.EmotionAnxious -> R.drawable.mascot_emotion_anxious
        MascotState.EmotionFrustrated -> R.drawable.mascot_emotion_frustrated
        MascotState.EmotionSad -> R.drawable.mascot_emotion_sad
        MascotState.EmotionHappy -> R.drawable.mascot_emotion_happy
        MascotState.EmotionTired -> R.drawable.mascot_emotion_tired
    }

    /**
     * Lottie asociado al estado, si existe. Devuelve `null` cuando todavía
     * no hay animación disponible y se debe usar el drawable estático.
     *
     * Cuando los `.json` lleguen a `res/raw/`, basta con reemplazar la rama
     * por `R.raw.mascot_breathing`, etc.
     */
    @RawRes
    fun lottieFor(state: MascotState): Int? = when (state) {
        MascotState.Breathing -> null
        MascotState.Grounding -> null
        MascotState.Stretching -> null
        MascotState.Reframing -> null
        MascotState.Resting -> null
        MascotState.Thinking -> null
        else -> null
    }

    /**
     * Mapea el id del ejercicio al estado de mascota apropiado. Los ids
     * provienen de `cl.ipvg.docentecalma.domain.rules.QuickExerciseCatalog`.
     */
    fun stateForExerciseId(exerciseId: String): MascotState = when (exerciseId) {
        "breathing_478" -> MascotState.Breathing
        "grounding_54321" -> MascotState.Grounding
        "active_pause" -> MascotState.Stretching
        "cognitive_reframe" -> MascotState.Reframing
        "micro_rest" -> MascotState.Resting
        else -> MascotState.Idle
    }
}
