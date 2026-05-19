package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.ChatMessageEntity
import cl.ipvg.docentecalma.domain.model.ChatMessage
import cl.ipvg.docentecalma.domain.model.ChatRole
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ChatMessageMapperTest {

    @Test
    fun `entity a dominio mapea rol por id`() {
        val entity = ChatMessageEntity(
            id = 1L,
            sessionId = "s-1",
            roleId = ChatRole.USER.id,
            content = "hola",
            createdAt = 1_700_000_000_000L
        )

        val domain = entity.toDomain()

        assertEquals(ChatRole.USER, domain.role)
        assertEquals("hola", domain.content)
        assertEquals("s-1", domain.sessionId)
    }

    @Test
    fun `entity con roleId desconocido cae a USER por contrato del enum`() {
        val entity = ChatMessageEntity(
            id = 2L,
            sessionId = "s-2",
            roleId = "desconocido",
            content = "x",
            createdAt = 0L
        )

        assertEquals(ChatRole.USER, entity.toDomain().role)
    }

    @Test
    fun `round-trip para rol MODEL preserva datos`() {
        val domain = ChatMessage(
            id = 9L,
            sessionId = "abc",
            role = ChatRole.MODEL,
            content = "respuesta",
            createdAt = Instant.ofEpochMilli(1_700_000_999_000L)
        )

        val roundTrip = domain.toEntity().toDomain()

        assertEquals(domain, roundTrip)
    }
}
