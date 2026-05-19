package cl.ipvg.docentecalma.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responder local determinista usado cuando:
 * - [GeminiSupportChatAi] falla (red, servidor, clave inválida, bloqueo),
 * - o se prefiere no consultar al modelo.
 *
 * Respeta las mismas reglas que el prompt de sistema de [AiConfig]:
 * - No diagnostica, no nombra trastornos, no recomienda medicamentos ni dosis.
 * - No da consejos médicos sobre síntomas físicos.
 * - Mantiene tono breve, empático y no motivacional.
 * - Siempre delega a profesionales ante señales de riesgo vía [RiskClassifier].
 *
 * Las heurísticas son intencionalmente acotadas: sugieren ejercicios ya
 * presentes en la app o nombran el recurso, sin dar instrucciones clínicas.
 */
@Singleton
class FallbackSupportChatAi @Inject constructor() : SupportChatAi {

    override suspend fun reply(
        history: List<SupportChatTurn>,
        userMessage: String
    ): AiResult {
        val risk = RiskClassifier.categorize(userMessage)
        if (risk != RiskCategory.NONE) {
            return AiResult.Success(
                text = RiskClassifier.safetyResponseFor(risk),
                fromFallback = true
            )
        }

        val text = userMessage.lowercase().trim()
        val reply = when {
            text.containsAny("estrés", "estres", "agobio", "sobrepasad", "saturad") ->
                "Suena a mucha carga encima. Si puedes, haz una pausa corta con la respiración " +
                    "4-7-8 de la app; al volver, elige una sola tarea para las próximas dos " +
                    "horas y deja el resto anotado para después."

            text.containsAny("ansi", "nervios", "inquiet") ->
                "Tiene sentido lo que cuentas. El ejercicio grounding 5-4-3-2-1 de la app " +
                    "ayuda a anclarte al presente unos minutos. Si esto se mantiene muy intenso " +
                    "o no te deja funcionar, es buena idea hablarlo con alguien de apoyo " +
                    "fuera de la app."

            text.containsAny("rabia", "enojo", "frustr", "molest") ->
                "La molestia que nombras es válida. Antes de actuar, una pausa activa breve " +
                    "desde la app puede bajar un poco la temperatura; muchas veces decidir " +
                    "con un poco más de calma cambia el resultado."

            text.containsAny("triste", "decaíd", "decaid", "vacío", "vacio", "desmotivad") ->
                "Gracias por compartirlo; ponerle palabras ya es cuidarse. El micro descanso " +
                    "de la app puede suavizar un momento difícil. Si el peso se alarga en " +
                    "el tiempo, buscar apoyo con una persona de confianza o un servicio " +
                    "especializado es un paso sensato."

            text.containsAny("cansad", "agotad", "sin energía", "sin energia", "fatig") ->
                "El cansancio que arrastra merece menos exigencia, no más. Hoy podrías quedarte " +
                    "con dos cosas realmente necesarias y posponer el resto sin culpa. Si el " +
                    "agotamiento es sobre todo físico y no mejora, conviene comentarlo con " +
                    "salud (no somos un servicio médico)."

            text.containsAny("aula", "curso", "estudiante", "alumn", "clase") ->
                "En el aula, una respiración antes de hablar suele ayudar. Una línea útil es " +
                    "describir la conducta sin etiquetar a la persona, dar una consigna corta " +
                    "y seguir con el contenido. Si se repite, anótalo y sigue el protocolo de " +
                    "tu instituto con jefe/a de carrera o registro académico."

            text.containsAny("hola", "buenas", "buenos días", "buen día") ->
                "Hola. Acá para escucharte con calma: ¿qué te gustaría ordenar o aliviar hoy?"

            else ->
                "Ahora mismo el asistente en línea no está disponible, pero sigo aquí con " +
                    "respuestas locales. En una frase, ¿qué estás sintiendo o qué situación " +
                    "te ocupa? Te propongo un siguiente paso breve. Esto es apoyo " +
                    "socioemocional en la app, no sustituye acompañamiento profesional."
        }

        return AiResult.Success(text = reply, fromFallback = true)
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { needle -> contains(needle) }
}
