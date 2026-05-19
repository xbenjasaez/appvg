package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.EmotionalCheckInEntity
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class EmotionalCheckInMapperTest {

    @Test
    fun `entity a dominio mapea todos los campos`() {
        val createdAt = Instant.parse("2025-10-18T12:34:56Z")
        val entity = EmotionalCheckInEntity(
            id = 42L,
            emotionId = Emotion.STRESS.id,
            intensity = 3,
            note = "carga alta hoy",
            createdAt = createdAt.toEpochMilli()
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals(Emotion.STRESS, domain.emotion)
        assertEquals(3, domain.intensity)
        assertEquals("carga alta hoy", domain.note)
        assertEquals(createdAt.toEpochMilli(), domain.createdAt.toEpochMilli())
    }

    @Test
    fun `entity a dominio conserva nota nula`() {
        val entity = EmotionalCheckInEntity(
            id = 1L,
            emotionId = Emotion.CALM.id,
            intensity = 2,
            note = null,
            createdAt = 0L
        )

        val domain = entity.toDomain()

        assertNull(domain.note)
        assertEquals(Emotion.CALM, domain.emotion)
    }

    @Test
    fun `dominio a entity y vuelta preserva datos (round-trip)`() {
        val domain = EmotionalCheckIn(
            id = 7L,
            emotion = Emotion.ANXIETY,
            intensity = 4,
            note = "pre-clase",
            createdAt = Instant.ofEpochMilli(1_700_000_000_000L)
        )

        val roundTrip = domain.toEntity().toDomain()

        assertEquals(domain, roundTrip)
    }

    @Test(expected = IllegalStateException::class)
    fun `entity con emotionId desconocido lanza error controlado`() {
        val entity = EmotionalCheckInEntity(
            id = 1L,
            emotionId = "inexistente",
            intensity = 3,
            note = null,
            createdAt = 0L
        )
        entity.toDomain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mapper respeta validacion de intensidad del dominio`() {
        val entity = EmotionalCheckInEntity(
            id = 1L,
            emotionId = Emotion.STRESS.id,
            intensity = 99,
            note = null,
            createdAt = 0L
        )
        entity.toDomain()
    }
}
