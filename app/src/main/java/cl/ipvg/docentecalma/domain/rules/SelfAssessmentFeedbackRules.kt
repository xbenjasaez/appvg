package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentNavHint
import cl.ipvg.docentecalma.domain.model.SelfAssessmentSuggestion
import cl.ipvg.docentecalma.util.DateTimeFormatters
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Resume el resultado, compara con el registro anterior y propone pasos concretos
 * dentro de la app (micromódulos). Lenguaje preventivo; sin etiquetas clínicas.
 */
@Singleton
class SelfAssessmentFeedbackRules @Inject constructor() {

    fun summaryForScore(total: Int): String {
        require(total in SelfAssessment.MIN_TOTAL..SelfAssessment.MAX_TOTAL)
        return when {
            total <= 8 ->
                "Tu registro apunta a una carga percibida bastante contenida en la última semana. " +
                    "Sigue cuidando rutinas breves de pausa y reconocimiento."
            total <= 13 ->
                "Hay señales de tensión moderada en tu autoinforme. Tiene sentido priorizar " +
                    "descanso micro, regulación y apoyo entre pares o recursos de la app."
            else ->
                "Tu autoinforme refleja una carga alta en esta semana. No estás solo/a: " +
                    "pequeños pasos (pausa, respiración, pedir apoyo) pueden aliviar el desgaste. " +
                    "Si esto se mantiene o afecta tu salud, busca apoyo de confianza o institucional."
        }
    }

    fun comparisonLine(currentTotal: Int, previous: SelfAssessment?): String? {
        val prev = previous ?: return null
        val delta = currentTotal - prev.totalScore
        val prevDate = DateTimeFormatters.short(prev.createdAt)
        return when {
            abs(delta) <= 1 ->
                "Respecto a tu registro anterior ($prevDate), el resultado es similar."
            delta > 1 ->
                "Respecto a tu registro anterior ($prevDate), la puntuación subió $delta puntos " +
                    "(autoinforme: más carga o tensión percibida)."
            else ->
                "Respecto a tu registro anterior ($prevDate), la puntuación bajó " +
                    "${-delta} puntos (autoinforme: algo menos de carga percibida)."
        }
    }

    fun suggestionsForScore(total: Int): List<SelfAssessmentSuggestion> = when {
        total <= 8 -> listOf(
            SelfAssessmentSuggestion(
                title = "Registrar cómo te sientes",
                description = "Un chequeo breve ayuda a detectar cambios antes de que escalen.",
                navHint = SelfAssessmentNavHint.EMOTIONAL_CHECK_IN
            ),
            SelfAssessmentSuggestion(
                title = "Micro descanso",
                description = "Mantén un ritual corto entre clases para sostener lo que ya te funciona.",
                navHint = SelfAssessmentNavHint.QUICK_EXERCISES
            )
        )
        total <= 13 -> listOf(
            SelfAssessmentSuggestion(
                title = "Ejercicio breve de regulación",
                description = "Prueba respiración 4-7-8 o una pausa activa de unos minutos.",
                navHint = SelfAssessmentNavHint.QUICK_EXERCISES
            ),
            SelfAssessmentSuggestion(
                title = "Chequeo emocional",
                description = "Pon nombre a la emoción e intensidad: facilita el autocuidado concreto.",
                navHint = SelfAssessmentNavHint.EMOTIONAL_CHECK_IN
            ),
            SelfAssessmentSuggestion(
                title = "Guía de aula",
                description = "Revisa un escenario parecido al tuyo: ideas prácticas sin culpa.",
                navHint = SelfAssessmentNavHint.CLASSROOM_GUIDANCE
            )
        )
        else -> listOf(
            SelfAssessmentSuggestion(
                title = "Ejercicios breves primero",
                description = "Prioriza algo corporal y corto (respiración, grounding, micro descanso).",
                navHint = SelfAssessmentNavHint.QUICK_EXERCISES
            ),
            SelfAssessmentSuggestion(
                title = "Chat de apoyo",
                description = "Si quieres ordenar ideas con conversación guiada (requiere internet).",
                navHint = SelfAssessmentNavHint.SUPPORT_CHAT
            ),
            SelfAssessmentSuggestion(
                title = "Guía de aula",
                description = "Herramientas para situaciones difíciles con estudiantes o entorno.",
                navHint = SelfAssessmentNavHint.CLASSROOM_GUIDANCE
            ),
            SelfAssessmentSuggestion(
                title = "Chequeo emocional",
                description = "Complementa esta autoevaluación con un registro al día.",
                navHint = SelfAssessmentNavHint.EMOTIONAL_CHECK_IN
            )
        )
    }
}
