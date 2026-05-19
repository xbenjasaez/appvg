package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import cl.ipvg.docentecalma.domain.model.ChatMessage
import cl.ipvg.docentecalma.domain.model.ChatRole
import java.time.Instant

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    sessionId = sessionId,
    role = ChatRole.fromId(roleId),
    content = content,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    roleId = role.id,
    content = content,
    createdAt = createdAt.toEpochMilli()
)
