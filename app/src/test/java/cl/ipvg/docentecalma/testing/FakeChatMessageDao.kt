package cl.ipvg.docentecalma.testing

import cl.ipvg.docentecalma.data.local.dao.ChatMessageDao
import cl.ipvg.docentecalma.data.local.dao.ChatSessionSummaryRow
import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [ChatMessageDao] para tests unitarios.
 */
class FakeChatMessageDao(
    initial: List<ChatMessageEntity> = emptyList()
) : ChatMessageDao {

    private val items = MutableStateFlow(initial)
    private var nextId: Long = (initial.maxOfOrNull { it.id } ?: 0L) + 1L

    fun currentItems(): List<ChatMessageEntity> = items.value

    override fun observeSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        items.map { list ->
            list.filter { it.sessionId == sessionId }
                .sortedBy { it.createdAt }
        }

    override fun observeSessionIds(): Flow<List<String>> =
        items.map { list ->
            list.groupBy { it.sessionId }
                .entries
                .sortedByDescending { (_, msgs) -> msgs.maxOf { it.createdAt } }
                .map { it.key }
        }

    override fun observeSessionSummaries(): Flow<List<ChatSessionSummaryRow>> =
        items.map { list ->
            list.groupBy { it.sessionId }
                .map { (sessionId, msgs) ->
                    val first = msgs.firstOrNull { it.roleId == "user" }
                    ChatSessionSummaryRow(
                        sessionId = sessionId,
                        messageCount = msgs.size,
                        firstCreatedAt = msgs.minOf { it.createdAt },
                        lastCreatedAt = msgs.maxOf { it.createdAt },
                        firstUserContent = first?.content
                    )
                }
                .sortedByDescending { it.lastCreatedAt }
        }

    override suspend fun getById(id: Long): ChatMessageEntity? =
        items.value.firstOrNull { it.id == id }

    override suspend fun insert(entity: ChatMessageEntity): Long {
        val assigned = if (entity.id == 0L) nextId++ else entity.id
        items.value = items.value + entity.copy(id = assigned)
        return assigned
    }

    override suspend fun deleteSession(sessionId: String) {
        items.value = items.value.filterNot { it.sessionId == sessionId }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }

    override fun observeCount(): Flow<Int> = items.map { it.size }
}
