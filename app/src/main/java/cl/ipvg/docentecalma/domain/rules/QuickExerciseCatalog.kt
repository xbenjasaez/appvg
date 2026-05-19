package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.QuickExercise

/**
 * Catálogo estático de ejercicios breves de regulación.
 * Es la fuente única para el motor de recomendaciones y para `QuickExercisesScreen`.
 */
object QuickExerciseCatalog {

    val BREATHING_478 = QuickExercise(
        id = "breathing_478",
        title = "Respiración 4-7-8",
        description = "Ejercicio de respiración que baja la activación fisiológica en minutos.",
        durationMinutes = 3,
        steps = listOf(
            "Inhala por la nariz contando mentalmente hasta 4.",
            "Retén el aire contando hasta 7.",
            "Exhala por la boca contando hasta 8.",
            "Repite el ciclo 4 veces."
        )
    )

    val GROUNDING_54321 = QuickExercise(
        id = "grounding_54321",
        title = "Grounding 5-4-3-2-1",
        description = "Anclaje sensorial para reducir ansiedad y volver al presente.",
        durationMinutes = 4,
        steps = listOf(
            "Identifica 5 cosas que puedes ver.",
            "Identifica 4 cosas que puedes tocar.",
            "Identifica 3 sonidos que puedes escuchar.",
            "Identifica 2 olores que puedes percibir.",
            "Identifica 1 sabor o sensación en tu boca."
        )
    )

    val ACTIVE_PAUSE = QuickExercise(
        id = "active_pause",
        title = "Pausa activa",
        description = "Movilización corporal breve para descargar tensión muscular.",
        durationMinutes = 2,
        steps = listOf(
            "Ponte de pie y estira brazos hacia el techo por 10 segundos.",
            "Rota los hombros hacia atrás 10 veces, luego hacia adelante 10 veces.",
            "Inclínate lateralmente a cada lado, manteniendo 10 segundos.",
            "Camina 10 pasos lentos respirando profundo."
        )
    )

    val COGNITIVE_REFRAME = QuickExercise(
        id = "cognitive_reframe",
        title = "Reencuadre cognitivo",
        description = "Observa el pensamiento difícil y formula una alternativa más realista.",
        durationMinutes = 5,
        steps = listOf(
            "Escribe o piensa la frase que te agobia ahora mismo.",
            "Pregúntate: ¿qué evidencia tengo de que sea así?",
            "Pregúntate: ¿qué le diría a una colega en mi lugar?",
            "Reformula la idea inicial en una versión más amable y realista."
        )
    )

    val MICRO_REST = QuickExercise(
        id = "micro_rest",
        title = "Micro descanso",
        description = "Pausa breve para reponer energía entre bloques de clase.",
        durationMinutes = 3,
        steps = listOf(
            "Aleja la vista de la pantalla y mira un punto lejano.",
            "Cierra los ojos y respira lento durante 1 minuto.",
            "Toma agua.",
            "Vuelve a la tarea con una sola prioridad clara."
        )
    )

    val all: List<QuickExercise> = listOf(
        BREATHING_478,
        GROUNDING_54321,
        ACTIVE_PAUSE,
        COGNITIVE_REFRAME,
        MICRO_REST
    )

    fun byId(id: String): QuickExercise? = all.firstOrNull { it.id == id }
}
