package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.Micromodule
import cl.ipvg.docentecalma.domain.model.MicromoduleBlock

/**
 * Catálogo local de micromódulos (3–5 min de lectura).
 *
 * Separación: definición aquí; estado en Room vía [MicromoduleProgressRepository].
 */
object MicromoduleCatalog {

    private val byId: Map<String, Micromodule> = listOf(
        Micromodule(
            id = "micro_pausa_cognitiva",
            title = "Pausa cognitiva de 90 segundos",
            lead = "Para cuando sientes la cabeza a mil antes o durante la clase.",
            estimatedMinutes = 3,
            blocks = listOf(
                MicromoduleBlock(
                    heading = "Qué es esta pausa",
                    lines = listOf(
                        "Es una pausa corta para recuperar atención, no para abandonar lo que estabas haciendo.",
                        "Tu cerebro rinde mejor si le das unos segundos sin nuevas tareas ni pantallas."
                    )
                ),
                MicromoduleBlock(
                    heading = "Cómo hacerla (90 segundos)",
                    lines = listOf(
                        "Mira un punto fijo, inhala contando hasta 4 y exhala hasta 6. Repítelo dos veces.",
                        "En voz baja, nombra tres cosas que ves y una que escuchas, sin juzgarlas.",
                        "Vuelve a tu tarea con una sola prioridad clara: “primero esto, después lo otro”."
                    )
                ),
                MicromoduleBlock(
                    heading = "Si estás en el aula",
                    lines = listOf(
                        "Puedes decir en alto: “Respiren conmigo un momento, seguimos” y pausar unos 20 segundos.",
                        "En esta pausa no abras correo ni redes: te distraen y no descansas de verdad."
                    )
                )
            ),
            relatedExerciseId = "grounding_54321"
        ),
        Micromodule(
            id = "micro_limite_respetuoso",
            title = "Límite claro, tono respetuoso",
            lead = "Cuando la tensión sube y necesitas poner orden sin humillar.",
            estimatedMinutes = 4,
            blocks = listOf(
                MicromoduleBlock(
                    heading = "Tres pasos cuando sube la tensión",
                    lines = listOf(
                        "Nombra el comportamiento que ves, no la persona (“hablan de más”, no “son malos”).",
                        "Recuerda la norma o expectativa en una frase.",
                        "Ofrece una salida concreta: “puedes X ahora o Y en un minuto”."
                    )
                ),
                MicromoduleBlock(
                    heading = "Frases que puedes decir",
                    lines = listOf(
                        "Úsalas tal cual o adaptándolas a tu asignatura.",
                        "“Necesito que bajemos el volumen para que todos escuchen la consigna.”",
                        "“El celular va guardado; si lo necesitas para la tarea, lo vemos al final.”"
                    )
                ),
                MicromoduleBlock(
                    heading = "Después del momento",
                    lines = listOf(
                        "Si hace falta seguir hablando, hazlo uno a uno, no frente a todo el curso.",
                        "Si sientes que pierdes el control, pide apoyo a un par, a tu jefe/a de " +
                            "carrera o a registro académico, según el protocolo de tu carrera."
                    )
                )
            ),
            relatedExerciseId = "cognitive_reframe"
        ),
        Micromodule(
            id = "micro_feedback_unico",
            title = "Cómo dar un feedback claro",
            lead = "Un comentario breve al estudiante sobre su trabajo o conducta, que puedas decir hoy mismo.",
            estimatedMinutes = 3,
            blocks = listOf(
                MicromoduleBlock(
                    heading = "En tres pasos",
                    lines = listOf(
                        "Sirve para orientar sin sermonear: di poco, pero concreto.",
                        "Primero, qué viste hoy (hecho o trabajo). Segundo, qué puede mejorar. " +
                            "Tercero, un ejemplo corto si ayuda.",
                        "Si lo dices en dos frases, mejor: se entiende y no se pierde."
                    )
                ),
                MicromoduleBlock(
                    heading = "Frase lista para usar",
                    lines = listOf(
                        "Ejemplo completo: “Vi que tu informe quedó a medias. Para avanzar, termina la " +
                            "introducción y revísala con la rúbrica.”",
                        "Plantilla: “Vi que [lo que ocurrió hoy]. Para avanzar, prueba [acción concreta].”",
                        "Di lo que pasó hoy, no “siempre te distraes” ni “nunca entregas a tiempo”: " +
                            "eso cierra la conversación."
                    )
                ),
                MicromoduleBlock(
                    heading = "Para terminar la conversación",
                    lines = listOf(
                        "Haz una sola pregunta: “¿Qué parte te costó más?” y escucha la respuesta.",
                        "Si no alcanza el tiempo, deja por escrito el acuerdo en un lugar que ambos vean."
                    )
                )
            )
        ),
        Micromodule(
            id = "micro_cierre_de_jornada",
            title = "Cerrar el día sin llevarte todo a casa",
            lead = "Un respiro para pasar del trabajo al resto de tu día, sin apuro.",
            estimatedMinutes = 4,
            blocks = listOf(
                MicromoduleBlock(
                    heading = "Antes de irte",
                    lines = listOf(
                        "Tres gestos breves para soltar el día y no llevarte todo en la cabeza.",
                        "Anota o recuerda una cosa que salió bien, aunque sea pequeña.",
                        "Escribe el pendiente en una línea (“mañana: revisar X”) y suéltalo por hoy.",
                        "Cambia de escena: camina un poco, toma agua o estira hombros."
                    )
                ),
                MicromoduleBlock(
                    heading = "Si hoy estás muy cansado/a",
                    lines = listOf(
                        "No tienes que resolverlo todo antes de salir: descansar también es cuidarte.",
                        "Si algo te queda dando vueltas, agenda un ratito para otro día y suelta lo que puede esperar."
                    )
                )
            ),
            relatedExerciseId = "micro_rest"
        ),
        Micromodule(
            id = "micro_apertura_de_clase",
            title = "Apertura de clase en calma",
            lead = "Primeros minutos que ordenan la atención del grupo.",
            estimatedMinutes = 5,
            blocks = listOf(
                MicromoduleBlock(
                    heading = "Primeros minutos de la clase",
                    lines = listOf(
                        "Sirven para que el grupo se ubique y empiece con foco.",
                        "Saluda breve y deja visible el objetivo (“Hoy cerramos…” o “Hoy partimos con…”).",
                        "Haz una pregunta o activación de 30–60 segundos; evita un monólogo largo al inicio.",
                        "Si hace falta, plantea una sola norma del día, concreta."
                    )
                ),
                MicromoduleBlock(
                    heading = "Si entras con energía baja",
                    lines = listOf(
                        "Reduce el plan a lo esencial: mejor un objetivo logrado que tres a medias.",
                        "Pide silencio con firma amable; tu ritmo marca el tono del espacio."
                    )
                ),
                MicromoduleBlock(
                    heading = "Recuerda",
                    lines = listOf(
                        "No necesitas fingir entusiasmo: la claridad tranquiliza más que actuar alegría a la fuerza."
                    )
                )
            ),
            relatedExerciseId = "breathing_478"
        )
    ).associateBy { it.id }

    val all: List<Micromodule> = byId.values.toList().sortedBy { it.id }

    fun byId(id: String): Micromodule = byId.getValue(id)

    fun byIdOrNull(id: String): Micromodule? = byId[id]

    /**
     * Micromódulo sugerido según la emoción del chequeo (integración con recomendaciones).
     */
    fun suggestedIdFor(emotion: Emotion): String? = when (emotion) {
        Emotion.STRESS, Emotion.ANXIETY, Emotion.ANGUST ->
            "micro_pausa_cognitiva"
        Emotion.ANGER ->
            "micro_limite_respetuoso"
        Emotion.SADNESS, Emotion.FRUSTRATION ->
            "micro_feedback_unico"
        Emotion.FATIGUE ->
            "micro_cierre_de_jornada"
        Emotion.CALM, Emotion.HAPPY ->
            "micro_apertura_de_clase"
    }
}
