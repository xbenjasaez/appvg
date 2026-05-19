package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.testing.FakeChatMessageDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    @Test
    fun `newSessionId retorna ids no vacios y distintos entre si`() {
        val repo = ChatRepository(FakeChatMessageDao())
        val a = repo.newSessionId()
        val b = repo.newSessionId()

        assertTrue(a.isNotBlank())
        assertNotEquals(a, b)
    }

    @Test
    fun `append persiste el mensaje y asigna id autogenerado`() = runTest {
        val dao = FakeChatMessageDao()
        val repo = ChatRepository(dao)

        val id = repo.append(
            sessionId = "s-1",
            role = ChatRole.USER,
            content = "hola",
            createdAt = Instant.ofEpochMilli(100L)
        )

        assertNotEquals(0L, id)
        val stored = dao.currentItems().single()
        assertEquals("s-1", stored.sessionId)
        assertEquals("user", stored.roleId)
        assertEquals("hola", stored.content)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `append rechaza contenido en blanco`() = runTest {
        val repo = ChatRepository(FakeChatMessageDao())
        repo.append("s-1", ChatRole.USER, "   ")
    }

    @Test
    fun `observeSession devuelve solo los mensajes de esa sesion ordenados por tiempo`() = runTest {
        val dao = FakeChatMessageDao()
        val repo = ChatRepository(dao)
        repo.append("a", ChatRole.USER, "a1", Instant.ofEpochMilli(10L))
        repo.append("a", ChatRole.MODEL, "a2", Instant.ofEpochMilli(20L))
        repo.append("b", ChatRole.USER, "b1", Instant.ofEpochMilli(15L))

        val sessionA = repo.observeSession("a").first()

        assertEquals(2, sessionA.size)
        assertEquals("a1", sessionA[0].content)
        assertEquals("a2", sessionA[1].content)
    }

    @Test
    fun `observeSessionSummaries agrega count y preview del primer USER`() = runTest {
        val dao = FakeChatMessageDao()
        val repo = ChatRepository(dao)
        repo.append("s-1", ChatRole.USER, "hola", Instant.ofEpochMilli(100L))
        repo.append("s-1", ChatRole.MODEL, "respuesta", Instant.ofEpochMilli(200L))
        repo.append("s-2", ChatRole.USER, "otra sesión", Instant.ofEpochMilli(50L))

        val summaries = repo.observeSessionSummaries().first()

        assertEquals(2, summaries.size)
        val s1 = summaries.single { it.sessionId == "s-1" }
        assertEquals(2, s1.messageCount)
        assertEquals("hola", s1.preview)
        val s2 = summaries.single { it.sessionId == "s-2" }
        assertEquals("otra sesión", s2.preview)
    }

    @Test
    fun `clearSession borra solo esa sesion`() = runTest {
        val dao = FakeChatMessageDao()
        val repo = ChatRepository(dao)
        repo.append("a", ChatRole.USER, "a1", Instant.ofEpochMilli(10L))
        repo.append("b", ChatRole.USER, "b1", Instant.ofEpochMilli(20L))

        repo.clearSession("a")

        assertTrue(repo.observeSession("a").first().isEmpty())
        assertEquals(1, repo.observeSession("b").first().size)
    }
}
