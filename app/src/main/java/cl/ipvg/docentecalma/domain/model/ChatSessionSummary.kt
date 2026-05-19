package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Resumen de una sesión de chat para la pantalla de historial.
 *
 * No contiene los mensajes completos por diseño: la lista detallada se carga
 * solo cuando el usuario abre una sesión específica.
 *
 * [preview] es el primer mensaje del usuario en la sesión, útil como título
 * visible. Puede ser `null` si la sesión no tiene aún un turno de usuario.
 */
data class ChatSessionSummary(
    val sessionId: String,
    val messageCount: Int,
    val firstAt: Instant,
    val lastAt: Instant,
    val preview: String?
)
