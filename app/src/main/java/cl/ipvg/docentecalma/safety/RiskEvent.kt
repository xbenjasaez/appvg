package cl.ipvg.docentecalma.safety

import cl.ipvg.docentecalma.ai.RiskCategory
import java.time.Instant

/**
 * Evento mínimo de riesgo emitido por el cliente cuando el `RiskClassifier`
 * detecta una categoría distinta de [RiskCategory.NONE].
 *
 * Diseño orientado a privacidad:
 * - No contiene el texto completo del mensaje del usuario.
 * - Solo lleva categoría, severidad inferida y un identificador seudónimo
 *   (`pseudoUserRef`) generado por sesión / instalación, nunca datos
 *   identificatorios directos (nombre, RUT, correo).
 * - `excerptHash` es un hash corto del extracto del mensaje, útil para
 *   deduplicar el mismo evento sin almacenar el contenido.
 *
 * Esta clase es el contrato estable para una futura integración de cola
 * remota (revisión humana). En esta fase los eventos solo se emiten al
 * [RiskEventSink] local.
 */
data class RiskEvent(
    val eventId: String,
    val category: RiskCategory,
    val severity: Severity,
    val occurredAt: Instant,
    val pseudoUserRef: String,
    val sessionRef: String,
    val excerptHash: String,
    val source: Source,
    val appVersion: String
) {
    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }

    enum class Source { SUPPORT_CHAT, OTHER }
}

/**
 * Mapea una categoría de riesgo a una severidad relativa para priorizar
 * revisión cuando se conecte una cola remota.
 */
internal fun RiskCategory.toSeverity(): RiskEvent.Severity = when (this) {
    RiskCategory.SELF_HARM,
    RiskCategory.HARM_TO_OTHERS -> RiskEvent.Severity.CRITICAL
    RiskCategory.ABUSE_OR_VIOLENCE -> RiskEvent.Severity.HIGH
    RiskCategory.ACUTE_CRISIS -> RiskEvent.Severity.MEDIUM
    RiskCategory.NONE -> RiskEvent.Severity.LOW
}
