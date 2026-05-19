package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.RecommendationHistoryEntity
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.RecommendationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class RecommendationHistoryMapperTest {

    @Test
    fun `entity a dominio mapea todos los campos`() {
        val entity = RecommendationHistoryEntity(
            id = 10L,
            checkInId = 99L,
            emotionId = Emotion.ANGER.id,
            intensity = 4,
            typeId = RecommendationType.PROFESSIONAL.id,
            summary = "Contención y derivación",
            acknowledged = true,
            createdAt = 1_700_000_000_000L
        )

        val domain = entity.toDomain()

        assertEquals(10L, domain.id)
        assertEquals(99L, domain.checkInId)
        assertEquals(Emotion.ANGER, domain.emotion)
        assertEquals(4, domain.intensity)
        assertEquals(RecommendationType.PROFESSIONAL, domain.type)
        assertEquals("Contención y derivación", domain.summary)
        assertEquals(true, domain.acknowledged)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.createdAt)
    }

    @Test
    fun `entity con typeId desconocido cae a IMMEDIATE por contrato del enum`() {
        val entity = RecommendationHistoryEntity(
            id = 1L,
            checkInId = null,
            emotionId = Emotion.CALM.id,
            intensity = 2,
            typeId = "no-existe",
            summary = "x",
            acknowledged = false,
            createdAt = 0L
        )

        val domain = entity.toDomain()

        assertEquals(RecommendationType.IMMEDIATE, domain.type)
        assertNull(domain.checkInId)
    }

    @Test
    fun `round-trip preserva datos`() {
        val domain = RecommendationHistory(
            id = 3L,
            checkInId = null,
            emotion = Emotion.FATIGUE,
            intensity = 3,
            type = RecommendationType.EXERCISE,
            summary = "Micro descanso",
            acknowledged = false,
            createdAt = Instant.ofEpochMilli(1_700_000_500_000L)
        )

        val roundTrip = domain.toEntity().toDomain()

        assertEquals(domain, roundTrip)
    }
}
