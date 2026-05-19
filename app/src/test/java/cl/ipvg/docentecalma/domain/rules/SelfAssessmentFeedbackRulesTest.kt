package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import cl.ipvg.docentecalma.domain.model.SelfAssessmentNavHint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SelfAssessmentFeedbackRulesTest {

    private val rules = SelfAssessmentFeedbackRules()

    @Test
    fun comparison_nullWhenNoPrevious() {
        assertNull(rules.comparisonLine(12, null))
    }

    @Test
    fun comparison_similarBand() {
        val prev = assessment(total = 10)
        val line = rules.comparisonLine(11, prev)
        assertNotNull(line)
        assertTrue(line!!.contains("similar", ignoreCase = true))
    }

    @Test
    fun comparison_increasedStrain() {
        val prev = assessment(total = 8)
        val line = rules.comparisonLine(12, prev)
        assertNotNull(line)
        assertTrue(line!!.contains("subió", ignoreCase = true))
    }

    @Test
    fun suggestions_includeExpectedDestinations_forHighScore() {
        val hints = rules.suggestionsForScore(18).map { it.navHint }.toSet()
        assertTrue(hints.contains(SelfAssessmentNavHint.QUICK_EXERCISES))
        assertTrue(hints.contains(SelfAssessmentNavHint.SUPPORT_CHAT))
    }

    @Test
    fun suggestions_lowScore_favorsCheckInAndExercises() {
        val hints = rules.suggestionsForScore(6).map { it.navHint }.toSet()
        assertEquals(
            setOf(
                SelfAssessmentNavHint.EMOTIONAL_CHECK_IN,
                SelfAssessmentNavHint.QUICK_EXERCISES
            ),
            hints
        )
    }

    private fun assessment(total: Int): SelfAssessment {
        val base = total / 4
        val rem = total % 4
        val answers = List(4) { i -> (base + if (i < rem) 1 else 0).coerceIn(1, 5) }
        return SelfAssessment(
            id = 1L,
            createdAt = Instant.parse("2026-01-01T12:00:00Z"),
            evaluationType = SelfAssessmentEvaluationType.PERIODIC,
            answers = answers,
            totalScore = answers.sum()
        )
    }
}
