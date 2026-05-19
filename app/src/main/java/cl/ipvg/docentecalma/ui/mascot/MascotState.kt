package cl.ipvg.docentecalma.ui.mascot

/**
 * Estados visuales del personaje. Cada estado representa una pose o una
 * animación que la mascota puede mostrar en pantalla.
 *
 * El mapeo a recurso (drawable o Lottie) vive en [MascotResources] para
 * mantener una sola tabla de verdad y permitir reemplazar los activos
 * sin tocar los call sites.
 *
 * Categorías:
 * - Branding: identidad de marca (splash, header del home, logros).
 * - Chat: estados ligados al chat de apoyo y a momentos de la IA.
 * - Exercises: animaciones para los ejercicios breves de regulación.
 * - Emotion: espejo emocional del chequeo, mapea a [cl.ipvg.docentecalma.domain.model.Emotion].
 */
internal sealed class MascotState {

    // Branding
    data object Greeting : MascotState()
    data object Idle : MascotState()
    data object Cheering : MascotState()

    // Chat / IA
    data object Listening : MascotState()
    data object Thinking : MascotState()
    data object Empathic : MascotState()
    data object OfflineSad : MascotState()
    data object ErrorState : MascotState()

    // Ejercicios breves (animados)
    data object Breathing : MascotState()
    data object Grounding : MascotState()
    data object Stretching : MascotState()
    data object Reframing : MascotState()
    data object Resting : MascotState()

    // Espejo emocional
    data object EmotionCalm : MascotState()
    data object EmotionAnxious : MascotState()
    data object EmotionFrustrated : MascotState()
    data object EmotionSad : MascotState()
    data object EmotionHappy : MascotState()
    data object EmotionTired : MascotState()
}
