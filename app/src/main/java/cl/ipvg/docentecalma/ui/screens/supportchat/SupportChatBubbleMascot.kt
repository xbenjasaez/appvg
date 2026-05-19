package cl.ipvg.docentecalma.ui.screens.supportchat

import cl.ipvg.docentecalma.domain.model.ChatMessage
import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.ui.mascot.MascotState

/**
 * Elige la pose del zorro junto a cada burbuja del asistente a partir del
 * **último mensaje del usuario** al que responde ese turno (heurística local,
 * sin llamadas extra al modelo). Si no hay texto previo o no encaja ningún
 * patrón, se usa [MascotState.Listening].
 */
internal fun mascotStateForAssistantBubble(userMessage: String?): MascotState {
    if (userMessage.isNullOrBlank()) return MascotState.Listening
    val t = userMessage.lowercase()

    fun containsAny(substrings: List<String>): Boolean = substrings.any { it in t }

    if (containsAny(
            listOf(
                "frustr", "enoj", "rabia", "molest", "furios", "irrit", "harto", "harta"
            )
        )
    ) {
        return MascotState.EmotionFrustrated
    }
    if (containsAny(
            listOf(
                "trist", "llor", "vacío", "vacio", "desanim", "depres", "soledad",
                "me siento solo", "me siento sola", "estoy solo", "estoy sola"
            )
        )
    ) {
        return MascotState.EmotionSad
    }
    if (containsAny(
            listOf(
                "ansiedad", "ansios", "nervios", "miedo", "angusti", "agobi", "estrés",
                "estres", "pánico", "panico", "atasc", "inquiet", "temblor"
            )
        )
    ) {
        return MascotState.EmotionAnxious
    }
    if (containsAny(
            listOf(
                "cansad", "exhaust", "agotad", "sueño", "sueno", "dormir", "fatiga",
                "sin energía", "sin energia", "reventad"
            )
        )
    ) {
        return MascotState.EmotionTired
    }
    if (containsAny(listOf("tranquil", "calma ", "calma,", "relajad", "en paz"))) {
        return MascotState.EmotionCalm
    }
    if (containsAny(
            listOf(
                "feliz", "contento", "contenta", "alegr", "genial", "gratific", "motivad"
            )
        )
    ) {
        return MascotState.EmotionHappy
    }
    if (containsAny(
            listOf(
                "difícil", "dificil", "complicado", "complicada", "pesad", "abrum",
                "desbord", "ya no puedo", "no doy abasto", "sobrepasa"
            )
        )
    ) {
        return MascotState.Empathic
    }
    return MascotState.Listening
}

/** Texto del usuario más reciente antes del mensaje del modelo en [modelIndex]. */
internal fun lastUserMessageBefore(messages: List<ChatMessage>, modelIndex: Int): String? {
    for (i in modelIndex - 1 downTo 0) {
        if (messages[i].role == ChatRole.USER) return messages[i].content
    }
    return null
}
