package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Mensaje del chat de apoyo en la capa de dominio.
 * [sessionId] permite agrupar y limpiar conversaciones sin borrar todo el historial.
 */
data class ChatMessage(
    val id: Long,
    val sessionId: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Instant
)
