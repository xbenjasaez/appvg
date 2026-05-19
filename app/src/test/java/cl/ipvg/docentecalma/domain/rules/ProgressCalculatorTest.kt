package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionCategory
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ProgressCalculatorTest {

    private val now: Instant = Instant.parse("2025-10-18T12:00:00Z")

    @Test
    fun `aggregate sobre lista vacia devuelve agregado vacio`() {
        val agg = ProgressCalculator.aggregate(emptyList(), windowDays = 7)

        assertEquals(0, agg.totalCheckIns)
        assertNull(agg.averageIntensity)
        assertNull(agg.mostFrequentEmotion)
        assertEquals(0, agg.mostFrequentCount)
        assertNull(agg.difficultShare)
        assertNull(agg.regulatedShare)
        assertTrue(agg.countByEmotion.isEmpty())
        assertTrue(agg.countByCategory.isEmpty())
    }

    @Test
    fun `aggregate calcula promedio y emocion mas frecuente`() {
        val list = listOf(
            checkIn(Emotion.STRESS, 4),
            checkIn(Emotion.STRESS, 2),
            checkIn(Emotion.CALM, 3)
        )

        val agg = ProgressCalculator.aggregate(list, windowDays = 30)

        assertEquals(3, agg.totalCheckIns)
        assertEquals(3.0, agg.averageIntensity!!, 0.0001)
        assertEquals(Emotion.STRESS, agg.mostFrequentEmotion)
        assertEquals(2, agg.mostFrequentCount)
    }

    @Test
    fun `aggregate agrupa por categoria correctamente`() {
        val list = listOf(
            checkIn(Emotion.STRESS, 3),      // DIFFICULT_HIGH_ACTIVATION
            checkIn(Emotion.FATIGUE, 4),     // DIFFICULT_LOW_ENERGY
            checkIn(Emotion.CALM, 2),        // REGULATED_POSITIVE
            checkIn(Emotion.HAPPY, 5)        // REGULATED_POSITIVE
        )

        val agg = ProgressCalculator.aggregate(list, windowDays = 30)

        assertEquals(1, agg.countByCategory[EmotionCategory.DIFFICULT_HIGH_ACTIVATION])
        assertEquals(1, agg.countByCategory[EmotionCategory.DIFFICULT_LOW_ENERGY])
        assertEquals(2, agg.countByCategory[EmotionCategory.REGULATED_POSITIVE])
    }

    @Test
    fun `aggregate expone difficultShare y regulatedShare como proporcion 0 a 1`() {
        val list = listOf(
            checkIn(Emotion.STRESS, 4),
            checkIn(Emotion.SADNESS, 3),
            checkIn(Emotion.CALM, 2),
            checkIn(Emotion.HAPPY, 5)
        )

        val agg = ProgressCalculator.aggregate(list, windowDays = 7)

        assertEquals(0.5, agg.difficultShare!!, 0.0001)
        assertEquals(0.5, agg.regulatedShare!!, 0.0001)
    }

    @Test
    fun `windowOf filtra por rango temporal`() {
        val old = checkIn(Emotion.STRESS, 3, now.minus(10, ChronoUnit.DAYS))
        val recent = checkIn(Emotion.CALM, 2, now.minus(2, ChronoUnit.DAYS))
        val all = listOf(old, recent)

        val last7 = ProgressCalculator.windowOf(all, lastDays = 7, now = now)

        assertEquals(1, last7.size)
        assertEquals(Emotion.CALM, last7.first().emotion)
    }

    @Test
    fun `weeklyBuckets produce bucketCount buckets y el mas reciente primero`() {
        val list = listOf(
            checkIn(Emotion.STRESS, 4, now.minus(1, ChronoUnit.DAYS)),
            checkIn(Emotion.STRESS, 3, now.minus(8, ChronoUnit.DAYS)),
            checkIn(Emotion.CALM, 2, now.minus(15, ChronoUnit.DAYS))
        )

        val buckets = ProgressCalculator.weeklyBuckets(list, bucketCount = 4, now = now)

        assertEquals(4, buckets.size)
        assertEquals(0, buckets[0].index)
        assertEquals(3, buckets[3].index)

        assertEquals(1, buckets[0].totalCheckIns)
        assertEquals(1, buckets[1].totalCheckIns)
        assertEquals(1, buckets[2].totalCheckIns)
        assertEquals(0, buckets[3].totalCheckIns)
    }

    @Test
    fun `weeklyBuckets deja averageIntensity null en semanas sin registros`() {
        val buckets = ProgressCalculator.weeklyBuckets(
            checkIns = emptyList(),
            bucketCount = 2,
            now = now
        )

        buckets.forEach { assertNull(it.averageIntensity) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `weeklyBuckets exige bucketCount mayor a cero`() {
        ProgressCalculator.weeklyBuckets(emptyList(), bucketCount = 0, now = now)
    }

    @Test
    fun `aggregate con un solo registro no lanza division por cero`() {
        val agg = ProgressCalculator.aggregate(
            listOf(checkIn(Emotion.ANXIETY, 5)),
            windowDays = 7
        )
        assertNotNull(agg.averageIntensity)
        assertEquals(5.0, agg.averageIntensity!!, 0.0001)
        assertEquals(1.0, agg.difficultShare!!, 0.0001)
        assertEquals(0.0, agg.regulatedShare!!, 0.0001)
    }

    @Test
    fun `distinctActiveLocalDays cuenta dias unicos en ventana`() {
        val zone = ZoneOffset.UTC
        val day0 = now.minus(1, ChronoUnit.DAYS)
        val day1 = now.minus(2, ChronoUnit.DAYS)
        val instants = listOf(
            day0,
            day0.plusMillis(1),
            day1
        )
        val n = ProgressCalculator.distinctActiveLocalDays(
            instants = instants,
            zone = zone,
            now = now,
            lastDays = 7
        )
        assertEquals(2, n)
    }

    @Test
    fun `mergePersonalTimeline ordena por fecha descendente`() {
        val older = now.minus(3, ChronoUnit.DAYS)
        val newer = now.minus(1, ChronoUnit.DAYS)
        val check = checkIn(Emotion.CALM, 2, at = older)
        val assessment = selfAssessment(at = newer, total = 12)
        val merged = ProgressCalculator.mergePersonalTimeline(
            checkIns = listOf(check),
            assessments = listOf(assessment),
            recommendations = emptyList(),
            maxItems = 10
        )
        assertTrue(merged[0] is ProgressCalculator.PersonalTimelineEntry.AssessmentEntry)
    }

    @Test
    fun `personalInsightLines respeta maximo tres frases`() {
        val list = List(8) { i ->
            checkIn(Emotion.STRESS, 4, now.minus(i.toLong(), ChronoUnit.DAYS))
        }
        val last7 = ProgressCalculator.aggregate(
            ProgressCalculator.windowOf(list, lastDays = 7, now = now),
            windowDays = 7
        )
        val lines = ProgressCalculator.personalInsightLines(
            activeDaysLast14 = 6,
            last7 = last7,
            checkIns = list,
            assessmentsSortedNewestFirst = emptyList(),
            exerciseRecommendationCount = 0,
            now = now,
            zone = ZoneOffset.UTC
        )
        assertTrue(lines.size <= 3)
        assertTrue(lines.isNotEmpty())
    }

    private fun checkIn(
        emotion: Emotion,
        intensity: Int,
        at: Instant = now
    ): EmotionalCheckIn = EmotionalCheckIn(
        id = 0L,
        emotion = emotion,
        intensity = intensity,
        note = null,
        createdAt = at
    )

    private fun selfAssessment(at: Instant, total: Int): SelfAssessment {
        val answers = when (total) {
            12 -> listOf(3, 3, 3, 3)
            else -> listOf(2, 2, 2, 2)
        }
        require(answers.sum() == total)
        return SelfAssessment(
            id = 1L,
            createdAt = at,
            evaluationType = SelfAssessmentEvaluationType.PERIODIC,
            answers = answers,
            totalScore = total
        )
    }
}
