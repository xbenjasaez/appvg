package cl.ipvg.docentecalma.ai

/**
 * Clasificador previo de riesgo sobre el mensaje del usuario.
 *
 * Cumple dos roles:
 * 1. Barrera defensiva antes de llamar al modelo, para NO depender solo de los
 *    filtros del proveedor en situaciones críticas.
 * 2. Selector del mensaje de derivación adecuado según la categoría detectada.
 *
 * Diseño intencional:
 * - Heurística simple basada en frases en español chileno, NO NLP.
 * - Sin regex complejas: mantenerlo predecible, testeable y fácil de auditar.
 * - Orden de evaluación prioriza el riesgo mayor (SELF_HARM > HARM_TO_OTHERS
 *   > ABUSE_OR_VIOLENCE > ACUTE_CRISIS).
 * - Todos los patrones están en minúsculas; se comparan contra el texto
 *   normalizado a minúscula.
 *
 * Los patrones buscan frases en primera persona (o con objeto explícito)
 * para reducir falsos positivos cuando el docente narra conductas ajenas.
 */
object RiskClassifier {

    private val SELF_HARM: List<String> = listOf(
        "suicid",
        "matarme",
        "quitarme la vida",
        "terminar con mi vida",
        "acabar con mi vida",
        "no quiero vivir",
        "no quiero seguir",
        "hacerme daño",
        "autolesion",
        "auto-lesion",
        "cortarme",
        "dañarme",
        "daño a mi mism",
        "daño a mí mism"
    )

    private val HARM_TO_OTHERS: List<String> = listOf(
        "quiero matar",
        "voy a matar",
        "quiero hacerle daño",
        "voy a hacerle daño",
        "voy a pegarle",
        "quiero pegarle",
        "voy a golpear",
        "quiero golpear",
        "ganas de pegarle",
        "le voy a hacer algo"
    )

    private val ABUSE_OR_VIOLENCE: List<String> = listOf(
        "me pega",
        "me pegó",
        "me pego",
        "me golpeó",
        "me golpea",
        "me amenaza",
        "me agrede",
        "abusa de mí",
        "abusa de mi",
        "abuso sexual",
        "violencia intrafamiliar",
        "violencia en mi casa",
        "me obliga"
    )

    private val ACUTE_CRISIS: List<String> = listOf(
        "crisis de pánico",
        "crisis de panico",
        "ataque de pánico",
        "ataque de panico",
        "no puedo respirar",
        "me voy a desmayar",
        "estoy colapsando",
        "ya no aguanto",
        "no puedo seguir",
        "no puedo más"
    )

    /**
     * Clasifica el mensaje en la primera categoría que aplique.
     * Si no aparece ningún patrón, retorna [RiskCategory.NONE].
     */
    fun categorize(userText: String): RiskCategory {
        if (userText.isBlank()) return RiskCategory.NONE
        val lower = userText.lowercase()
        return when {
            SELF_HARM.anyContainedIn(lower) -> RiskCategory.SELF_HARM
            HARM_TO_OTHERS.anyContainedIn(lower) -> RiskCategory.HARM_TO_OTHERS
            ABUSE_OR_VIOLENCE.anyContainedIn(lower) -> RiskCategory.ABUSE_OR_VIOLENCE
            ACUTE_CRISIS.anyContainedIn(lower) -> RiskCategory.ACUTE_CRISIS
            else -> RiskCategory.NONE
        }
    }

    /** Atajo: true si el mensaje cae en alguna categoría distinta de NONE. */
    fun isBlocking(userText: String): Boolean =
        categorize(userText) != RiskCategory.NONE

    /**
     * Respuesta segura para cada categoría. Nunca ofrece técnicas largas,
     * nunca diagnostica y siempre deriva a ayuda profesional o servicios
     * de emergencia cuando corresponde.
     */
    fun safetyResponseFor(category: RiskCategory): String = when (category) {
        RiskCategory.SELF_HARM -> SELF_HARM_RESPONSE
        RiskCategory.HARM_TO_OTHERS -> HARM_TO_OTHERS_RESPONSE
        RiskCategory.ABUSE_OR_VIOLENCE -> ABUSE_RESPONSE
        RiskCategory.ACUTE_CRISIS -> ACUTE_CRISIS_RESPONSE
        RiskCategory.NONE -> GENERIC_SAFETY_REMINDER
    }

    private fun List<String>.anyContainedIn(text: String): Boolean =
        any { phrase -> phrase in text }

    // --- Mensajes ---------------------------------------------------------

    private val SELF_HARM_RESPONSE: String = buildString {
        append("Gracias por contarlo, lo que describes es importante y mereces ")
        append("acompañamiento ahora. Este chat es de apoyo y no reemplaza ")
        append("atención profesional. Si estás en riesgo en este momento, llama a ")
        append("Salud Responde al 600 360 7777, acude al servicio de ")
        append("urgencias más cercano o pide a alguien de confianza que te acompañe. ")
        append("Cuando puedas, también puedes apoyarte en los conductos regulares ")
        append("del IP Virginio Gómez: hablar con tu jefe/a de carrera ")
        append("y revisar en la intranet el canal de denuncias y acude a registro académico ")
        append("cuando corresponda gestión formal.")
    }

    private val HARM_TO_OTHERS_RESPONSE: String = buildString {
        append("Gracias por la confianza de contarlo. Impulsos así de intensos ")
        append("suelen aparecer cuando hay mucha sobrecarga; cuidarte ahora es lo primero. ")
        append("Antes de cualquier acción, toma distancia física de la situación y ")
        append("no tomes decisiones en este estado. Como siguiente paso seguro, ")
        append("habla con un profesional (Salud Responde 600 360 7777) y, si la ")
        append("situación involucra a un estudiante o al ámbito laboral, sigue los ")
        append("conductos regulares del IP Virginio Gómez: jefe/a de ")
        append("carrera, registro académico y el botón de denuncias disponible en la intranet del instituto. ")
        append("Si hay un riesgo inminente para alguien, contacta a Carabineros al 133.")
    }

    private val ABUSE_RESPONSE: String = buildString {
        append("Lamento que estés pasando por esto y gracias por confiar. Lo que ")
        append("describes es serio y no es tu responsabilidad resolverlo sola o solo. ")
        append("Puedes llamar al 1455 de SernamEG (violencia contra mujeres), ")
        append("al 800 200 818 de Fonoinfancia si hay menores involucrados, o a ")
        append("Carabineros al 133 si hay riesgo inmediato. También puedes apoyarte ")
        append("en los conductos regulares del IP Virginio Gómez (jefe/a ")
        append("de carrera y registro académico), también considerar avisar al guardia de seguridad")
        append(", y en el canal de denuncias disponible en la intranet, ")
        append("además de alguien cercano de confianza.")
    }

    private val ACUTE_CRISIS_RESPONSE: String = buildString {
        append("Estoy aquí contigo. Si puedes, ubica un lugar seguro y respira lento: ")
        append("inhala 4 segundos, mantén 4, exhala 6, por unos minutos. Este chat ")
        append("no reemplaza atención profesional: si la sensación no cede o se ")
        append("intensifica, llama a Salud Responde al 600 360 7777 o acude a ")
        append("urgencias. Si tienes a alguien cerca, avísale cómo te estás sintiendo.")
    }

    private val GENERIC_SAFETY_REMINDER: String = buildString {
        append("Recuerda que este chat es un apoyo socioemocional y no reemplaza ")
        append("atención profesional. Si lo necesitas, puedes contactar a Salud ")
        append("Responde 600 360 7777, o seguir los conductos regulares del IP ")
        append("Virginio Gómez (Jefe/a de carrera) y revisar el canal de ")
        append("denuncias en la intranet y registro académico cuando corresponda.")
    }
}
