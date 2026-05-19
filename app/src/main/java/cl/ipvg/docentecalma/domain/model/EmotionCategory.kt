package cl.ipvg.docentecalma.domain.model

/**
 * Categoriza emociones por valencia y activación. Se usa como llave del motor
 * de recomendaciones y de los agregados de progreso.
 *
 * - [DIFFICULT_HIGH_ACTIVATION]: emociones difíciles con alta activación fisiológica
 *   (estrés, ansiedad, enojo, frustración).
 * - [DIFFICULT_LOW_ENERGY]: emociones difíciles con baja energía o retiro
 *   (angustia, tristeza, cansancio).
 * - [REGULATED_POSITIVE]: estados regulados o de bienestar (calma, felicidad).
 */
enum class EmotionCategory {
    DIFFICULT_HIGH_ACTIVATION,
    DIFFICULT_LOW_ENERGY,
    REGULATED_POSITIVE
}
