package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion

/**
 * Plantilla base de contenido por emoción usada por el `RecommendationEngine`.
 *
 * Deja fuera lo que depende de la intensidad (shortMessage, severity) para que
 * el motor lo componga. Mantener este catálogo separado permite editar textos
 * sin tocar la lógica del motor ni sus tests.
 */
data class EmotionRule(
    val title: String,
    val immediateAction: String,
    val breathingSuggestion: String,
    val whatToAvoid: String,
    val optionalPedagogicalTip: String?,
    val suggestedExerciseId: String?
)

object EmotionRuleCatalog {

    private val rules: Map<Emotion, EmotionRule> = mapOf(
        Emotion.STRESS to EmotionRule(
            title = "Estrés: contención rápida",
            immediateAction = "Haz una pausa de 3 minutos y aleja las pantallas.",
            breathingSuggestion = "Respiración 4-7-8 por 3 ciclos.",
            whatToAvoid = "Encadenar tareas sin pausa.",
            optionalPedagogicalTip = "Recorta tu próximo bloque a un solo objetivo claro.",
            suggestedExerciseId = QuickExerciseCatalog.BREATHING_478.id
        ),
        Emotion.ANXIETY to EmotionRule(
            title = "Ansiedad: vuelve al presente",
            immediateAction = "Aplica grounding 5-4-3-2-1 antes de seguir.",
            breathingSuggestion = "Inhala 4, exhala 6. Tres ciclos.",
            whatToAvoid = "Adelantar escenarios negativos en tu mente.",
            optionalPedagogicalTip = "Define qué entrega sí esperas hoy de tu próxima clase.",
            suggestedExerciseId = QuickExerciseCatalog.GROUNDING_54321.id
        ),
        Emotion.ANGUST to EmotionRule(
            title = "Angustia: date un respiro",
            immediateAction = "Ve a un espacio tranquilo y bebe agua.",
            breathingSuggestion = "Respiración diafragmática suave por 1 minuto.",
            whatToAvoid = "Tomar decisiones difíciles en este momento.",
            optionalPedagogicalTip = "Cierra la jornada con lo esencial y delega el resto si puedes.",
            suggestedExerciseId = QuickExerciseCatalog.BREATHING_478.id
        ),
        Emotion.ANGER to EmotionRule(
            title = "Enojo: baja la activación",
            immediateAction = "Pausa 2 minutos antes de responder a quien sea.",
            breathingSuggestion = "Inhala 4, exhala 8. Tres ciclos.",
            whatToAvoid = "Responder mensajes o tomar decisiones en caliente.",
            optionalPedagogicalTip = "Si hubo conflicto en clase, abórdalo en privado y más tarde.",
            suggestedExerciseId = QuickExerciseCatalog.BREATHING_478.id
        ),
        Emotion.SADNESS to EmotionRule(
            title = "Tristeza: permítete sentir",
            immediateAction = "Pausa 5 minutos con algo cálido cerca.",
            breathingSuggestion = "Respiración lenta y consciente por 2 minutos.",
            whatToAvoid = "Auto-exigirte alta productividad ahora.",
            optionalPedagogicalTip = "Prioriza hoy solo dos tareas pedagógicas clave.",
            suggestedExerciseId = QuickExerciseCatalog.COGNITIVE_REFRAME.id
        ),
        Emotion.FRUSTRATION to EmotionRule(
            title = "Frustración: redefine la meta",
            immediateAction = "Aléjate 3 minutos del problema y respira.",
            breathingSuggestion = "Inhala 4, retén 4, exhala 4. Tres ciclos.",
            whatToAvoid = "Persistir en algo que hoy no está funcionando.",
            optionalPedagogicalTip = "Revisa si el objetivo del bloque es alcanzable o conviene simplificarlo.",
            suggestedExerciseId = QuickExerciseCatalog.COGNITIVE_REFRAME.id
        ),
        Emotion.FATIGUE to EmotionRule(
            title = "Cansancio: recupera energía",
            immediateAction = "Párate, estírate un minuto y toma agua.",
            breathingSuggestion = "Respiración consciente por 1 minuto.",
            whatToAvoid = "Sumar tareas nuevas hoy.",
            optionalPedagogicalTip = "Reordena tu próxima clase incluyendo una actividad más autónoma.",
            suggestedExerciseId = QuickExerciseCatalog.MICRO_REST.id
        ),
        Emotion.CALM to EmotionRule(
            title = "Calma: mantén el eje",
            immediateAction = "Anota una cosa que te ayudó a estar así hoy.",
            breathingSuggestion = "Respiración natural por 1 minuto para anclarla.",
            whatToAvoid = "Saltar a la próxima tarea sin registrar el momento.",
            optionalPedagogicalTip = "Aprovecha este estado para planificar una clase desafiante.",
            suggestedExerciseId = null
        ),
        Emotion.HAPPY to EmotionRule(
            title = "Felicidad: amplifica y comparte",
            immediateAction = "Dedica un minuto a saborear lo que estás sintiendo.",
            breathingSuggestion = "Respiración relajada por 1 minuto, suelta tensión al exhalar.",
            whatToAvoid = "Sobrecargarte de trabajo solo porque tienes energía disponible.",
            optionalPedagogicalTip = "Usa esta energía para crear algo nuevo o animar al grupo.",
            suggestedExerciseId = null
        )
    )

    fun ruleFor(emotion: Emotion): EmotionRule = rules.getValue(emotion)
}
