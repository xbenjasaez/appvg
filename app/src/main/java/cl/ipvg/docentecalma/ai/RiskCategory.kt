package cl.ipvg.docentecalma.ai

/**
 * Categorías de riesgo detectables previo a llamar al modelo.
 *
 * - [NONE]: no se detectan señales, el mensaje puede enviarse al modelo con
 *   normalidad.
 * - [SELF_HARM]: ideación, planes o intentos de hacerse daño.
 * - [HARM_TO_OTHERS]: intención o impulso expresado de dañar a otra persona.
 * - [ABUSE_OR_VIOLENCE]: el usuario describe ser víctima de violencia o abuso.
 * - [ACUTE_CRISIS]: crisis aguda (pánico severo, disociación, colapso).
 *
 * Toda categoría distinta de [NONE] bloquea la llamada al modelo y produce una
 * respuesta segura pre-redactada vía [RiskClassifier.safetyResponseFor].
 */
enum class RiskCategory {
    NONE,
    SELF_HARM,
    HARM_TO_OTHERS,
    ABUSE_OR_VIOLENCE,
    ACUTE_CRISIS
}
