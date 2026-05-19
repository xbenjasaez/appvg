package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.testing.FakeEmotionalCheckInDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EmotionalRepositoryTest {

    @Test
    fun `save persiste la entity y retorna id autogenerado`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)

        val id = repo.save(
            emotion = Emotion.STRESS,
            intensity = 3,
            note = "clase dura",
            createdAt = Instant.ofEpochMilli(1_000L)
        )

        assertNotEquals(0L, id)
        val saved = dao.currentItems().single()
        assertEquals("stress", saved.emotionId)
        assertEquals(3, saved.intensity)
        assertEquals("clase dura", saved.note)
    }

    @Test
    fun `save recorta la nota en blanco a null`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)

        repo.save(emotion = Emotion.CALM, intensity = 2, note = "   ")

        assertNull(dao.currentItems().single().note)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `save rechaza intensidad fuera de rango`() = runTest {
        val repo = EmotionalRepository(FakeEmotionalCheckInDao())
        repo.save(emotion = Emotion.STRESS, intensity = 10, note = null)
    }

    @Test
    fun `observeAll proyecta a dominio`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)
        repo.save(Emotion.ANXIETY, 4, null, Instant.ofEpochMilli(10L))
        repo.save(Emotion.CALM, 2, null, Instant.ofEpochMilli(20L))

        val list = repo.observeAll().first()

        assertEquals(2, list.size)
        assertEquals(Emotion.CALM, list.first().emotion)
        assertEquals(Emotion.ANXIETY, list.last().emotion)
    }

    @Test
    fun `observeRange filtra por rango temporal`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)
        repo.save(Emotion.STRESS, 3, null, Instant.ofEpochMilli(100L))
        repo.save(Emotion.CALM, 2, null, Instant.ofEpochMilli(500L))

        val filtered = repo.observeRange(
            from = Instant.ofEpochMilli(300L),
            to = Instant.ofEpochMilli(1000L)
        ).first()

        assertEquals(1, filtered.size)
        assertEquals(Emotion.CALM, filtered.single().emotion)
    }

    @Test
    fun `observeLatest emite el mas reciente`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)
        repo.save(Emotion.STRESS, 3, null, Instant.ofEpochMilli(100L))
        repo.save(Emotion.CALM, 2, null, Instant.ofEpochMilli(500L))

        val latest = repo.observeLatest().first()

        assertEquals(Emotion.CALM, latest?.emotion)
    }

    @Test
    fun `observeEmotionCounts agrupa correctamente`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)
        repo.save(Emotion.STRESS, 3, null, Instant.ofEpochMilli(100L))
        repo.save(Emotion.STRESS, 2, null, Instant.ofEpochMilli(200L))
        repo.save(Emotion.CALM, 2, null, Instant.ofEpochMilli(300L))

        val counts = repo.observeEmotionCounts(
            from = Instant.ofEpochMilli(0L),
            to = Instant.ofEpochMilli(1_000L)
        ).first()

        assertEquals(2, counts[Emotion.STRESS])
        assertEquals(1, counts[Emotion.CALM])
    }

    @Test
    fun `delete elimina por id sin tocar los demas`() = runTest {
        val dao = FakeEmotionalCheckInDao()
        val repo = EmotionalRepository(dao)
        val id1 = repo.save(Emotion.STRESS, 3, null, Instant.ofEpochMilli(100L))
        repo.save(Emotion.CALM, 2, null, Instant.ofEpochMilli(200L))

        repo.delete(id1)

        val remaining = repo.observeAll().first()
        assertEquals(1, remaining.size)
        assertTrue(remaining.none { it.id == id1 })
    }
}
