package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.dao.ChatMessageDao
import cl.ipvg.docentecalma.data.local.dao.ChatSessionSummaryRow
import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import cl.ipvg.docentecalma.domain.mapper.toDomain
import cl.ipvg.docentecalma.domain.model.ChatMessage
import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.domain.model.ChatSessionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistencia del chat de apoyo. La lógica de red y generación vive en la capa ai/
 * y el ViewModel orquesta repositorio + AiRepository.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val dao: ChatMessageDao
) {

    fun observeSession(sessionId: String): Flow<List<ChatMessage>> =
        dao.observeSession(sessionId).map { list -> list.map { it.toDomain() } }

    fun observeSessionIds(): Flow<List<String>> = dao.observeSessionIds()

    /**
     * Resúmenes de todas las sesiones (sin los mensajes detallados). Útil para
     * Historial, que lista conversaciones ordenadas por actividad reciente.
     */
    fun observeSessionSummaries(): Flow<List<ChatSessionSummary>> =
        dao.observeSessionSummaries().map { rows -> rows.map { it.toDomain() } }

    private fun ChatSessionSummaryRow.toDomain(): ChatSessionSummary =
        ChatSessionSummary(
            sessionId = sessionId,
            messageCount = messageCount,
            firstAt = Instant.ofEpochMilli(firstCreatedAt),
            lastAt = Instant.ofEpochMilli(lastCreatedAt),
            preview = firstUserContent?.trim()?.takeIf { it.isNotEmpty() }
        )

    suspend fun append(
        sessionId: String,
        role: ChatRole,
        content: String,
        createdAt: Instant = Instant.now()
    ): Long {
        require(content.isNotBlank()) { "content no puede estar en blanco" }
        val entity = ChatMessageEntity(
            sessionId = sessionId,
            roleId = role.id,
            content = content.trim(),
            createdAt = createdAt.toEpochMilli()
        )
        return dao.insert(entity)
    }

    suspend fun clearSession(sessionId: String) = dao.deleteSession(sessionId)

    suspend fun deleteAll() = dao.deleteAll()

    fun newSessionId(): String = UUID.randomUUID().toString()
}
