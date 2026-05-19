package cl.ipvg.docentecalma.ui.mascot

import cl.ipvg.docentecalma.domain.model.Emotion

/**
 * Traduce una [Emotion] del catálogo del dominio a un [MascotState] que
 * la mascota puede mostrar. Permite que la cara del personaje refleje
 * la emoción seleccionada en el chequeo emocional sin acoplar la UI a
 * los identificadores específicos del catálogo.
 *
 * Si la emoción aún no está seleccionada, devuelve [MascotState.Idle]
 * para no dejar el espacio vacío.
 */
internal object MascotEmotionMapper {

    fun fromEmotion(emotion: Emotion?): MascotState = when (emotion) {
        Emotion.STRESS,
        Emotion.ANXIETY -> MascotState.EmotionAnxious
        Emotion.ANGUST,
        Emotion.SADNESS -> MascotState.EmotionSad
        Emotion.ANGER,
        Emotion.FRUSTRATION -> MascotState.EmotionFrustrated
        Emotion.FATIGUE -> MascotState.EmotionTired
        Emotion.CALM -> MascotState.EmotionCalm
        Emotion.HAPPY -> MascotState.EmotionHappy
        null -> MascotState.Idle
    }
}
