package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionCategory

/**
 * Mapper centralizado de emociones a texto visible en español.
 *
 * Exponer el mapeo aquí permite que la UI, las notificaciones y los resúmenes
 * de historial/progreso usen el mismo vocabulario sin depender del `label`
 * hard-codeado en el enum.
 */
object EmotionLabels {

    fun displayName(emotion: Emotion): String = when (emotion) {
        Emotion.STRESS -> "Estrés"
        Emotion.ANXIETY -> "Ansiedad"
        Emotion.ANGUST -> "Angustia"
        Emotion.ANGER -> "Enojo"
        Emotion.SADNESS -> "Tristeza"
        Emotion.FRUSTRATION -> "Frustración"
        Emotion.FATIGUE -> "Cansancio"
        Emotion.CALM -> "Calma"
        Emotion.HAPPY -> "Feliz"
    }

    fun shortDescription(emotion: Emotion): String = when (emotion) {
        Emotion.STRESS -> "Tensión acumulada por demandas."
        Emotion.ANXIETY -> "Inquietud o preocupación anticipatoria."
        Emotion.ANGUST -> "Malestar opresivo difícil de nombrar."
        Emotion.ANGER -> "Reacción intensa frente a algo percibido injusto."
        Emotion.SADNESS -> "Estado bajo, con ganas de pausar."
        Emotion.FRUSTRATION -> "Bloqueo entre lo esperado y lo posible."
        Emotion.FATIGUE -> "Agotamiento físico o mental."
        Emotion.CALM -> "Serenidad, cuerpo relajado."
        Emotion.HAPPY -> "Ánimo positivo, energía disponible."
    }

    fun categoryLabel(category: EmotionCategory): String = when (category) {
        EmotionCategory.DIFFICULT_HIGH_ACTIVATION -> "Difícil con alta activación"
        EmotionCategory.DIFFICULT_LOW_ENERGY -> "Difícil con baja energía"
        EmotionCategory.REGULATED_POSITIVE -> "Regulada o positiva"
    }

    fun intensityLabel(intensity: Int): String = when (intensity.coerceIn(1, 5)) {
        1 -> "Muy leve"
        2 -> "Leve"
        3 -> "Moderada"
        4 -> "Intensa"
        else -> "Muy intensa"
    }
}

val Emotion.displayName: String get() = EmotionLabels.displayName(this)
val Emotion.shortDescription: String get() = EmotionLabels.shortDescription(this)
val EmotionCategory.label: String get() = EmotionLabels.categoryLabel(this)
