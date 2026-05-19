package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Autoevaluación guardada localmente: cuatro ítems Likert (1–5) sobre carga percibida
 * en la última semana. El total es la suma (rango 4–20); no constituye diagnóstico.
 */
data class SelfAssessment(
    val id: Long,
    val createdAt: Instant,
    val evaluationType: SelfAssessmentEvaluationType,
    val answers: List<Int>,
    val totalScore: Int
) {
    init {
        require(answers.size == QUESTION_COUNT) {
            "Se esperaban $QUESTION_COUNT respuestas, llegaron ${answers.size}"
        }
        require(answers.all { it in SCORE_RANGE }) {
            "Cada respuesta debe estar en $SCORE_RANGE"
        }
    }

    companion object {
        const val QUESTION_COUNT: Int = 4
        val SCORE_RANGE: IntRange = 1..5
        val MIN_TOTAL: Int = QUESTION_COUNT * SCORE_RANGE.first
        val MAX_TOTAL: Int = QUESTION_COUNT * SCORE_RANGE.last
    }
}
