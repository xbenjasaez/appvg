package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.model.MicromoduleUserProgress
import cl.ipvg.docentecalma.domain.model.PilotEffectivenessVerdict
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PilotEffectivenessCalculatorTest {

    private val t0 = Instant.parse("2025-01-01T12:00:00Z")
    private val t1 = Instant.parse("2025-01-15T12:00:00Z")

    @Test
    fun insufficient_when_no_signals() {
        val s = PilotEffectivenessCalculator.build(
            assessments = emptyList(),
            moduleProgress = emptyList(),
            modulesTotal = 5,
            activeDistinctDaysLast14 = 0,
            distinctPilotDaysAllTime = 0,
            totalPilotEvents = 0L,
            feedbackSatisfaction = null,
            feedbackUsefulness = null,
            feedbackEase = null
        )
        assertEquals(PilotEffectivenessVerdict.INSUFFICIENT_EVIDENCE, s.verdict)
    }

    @Test
    fun awaiting_second_when_only_one_assessment() {
        val a = assessment(1L, t0, SelfAssessmentEvaluationType.INITIAL, listOf(4, 4, 4, 4))
        val s = PilotEffectivenessCalculator.build(
            assessments = listOf(a),
            moduleProgress = emptyList(),
            modulesTotal = 5,
            activeDistinctDaysLast14 = 6,
            distinctPilotDaysAllTime = 8,
            totalPilotEvents = 20L,
            feedbackSatisfaction = null,
            feedbackUsefulness = null,
            feedbackEase = null
        )
        assertEquals(PilotEffectivenessVerdict.AWAITING_SECOND_SELF_ASSESSMENT, s.verdict)
    }

    @Test
    fun initial_positive_when_improvement_sat_use_and_sustained_days() {
        val base = assessment(1L, t0, SelfAssessmentEvaluationType.INITIAL, listOf(5, 5, 5, 5))
        val latest = assessment(2L, t1, SelfAssessmentEvaluationType.PERIODIC, listOf(3, 3, 3, 3))
        val s = PilotEffectivenessCalculator.build(
            assessments = listOf(base, latest),
            moduleProgress = emptyList(),
            modulesTotal = 5,
            activeDistinctDaysLast14 = 5,
            distinctPilotDaysAllTime = 10,
            totalPilotEvents = 30L,
            feedbackSatisfaction = 5,
            feedbackUsefulness = 5,
            feedbackEase = 5
        )
        assertTrue(s.meaningfulImprovement)
        assertEquals(PilotEffectivenessVerdict.INITIAL_POSITIVE_SIGNALS, s.verdict)
    }

    @Test
    fun usage_without_shift_when_no_meaningful_improvement_but_sustained() {
        val base = assessment(1L, t0, SelfAssessmentEvaluationType.INITIAL, listOf(4, 4, 4, 4))
        val latest = assessment(2L, t1, SelfAssessmentEvaluationType.PERIODIC, listOf(4, 4, 3, 4))
        val s = PilotEffectivenessCalculator.build(
            assessments = listOf(base, latest),
            moduleProgress = emptyList(),
            modulesTotal = 5,
            activeDistinctDaysLast14 = 5,
            distinctPilotDaysAllTime = 10,
            totalPilotEvents = 40L,
            feedbackSatisfaction = 5,
            feedbackUsefulness = 5,
            feedbackEase = 5
        )
        assertFalse(s.meaningfulImprovement)
        assertEquals(PilotEffectivenessVerdict.USAGE_WITHOUT_DEMONSTRATED_SHIFT, s.verdict)
    }

    @Test
    fun positive_trend_incomplete_without_feedback() {
        val base = assessment(1L, t0, SelfAssessmentEvaluationType.INITIAL, listOf(5, 5, 5, 5))
        val latest = assessment(2L, t1, SelfAssessmentEvaluationType.PERIODIC, listOf(3, 3, 3, 3))
        val s = PilotEffectivenessCalculator.build(
            assessments = listOf(base, latest),
            moduleProgress = emptyList(),
            modulesTotal = 5,
            activeDistinctDaysLast14 = 5,
            distinctPilotDaysAllTime = 10,
            totalPilotEvents = 30L,
            feedbackSatisfaction = null,
            feedbackUsefulness = null,
            feedbackEase = null
        )
        assertTrue(s.meaningfulImprovement)
        assertEquals(PilotEffectivenessVerdict.POSITIVE_TREND_INCOMPLETE_PICTURE, s.verdict)
    }

    private fun assessment(
        id: Long,
        at: Instant,
        type: SelfAssessmentEvaluationType,
        answers: List<Int>
    ): SelfAssessment = SelfAssessment(
        id = id,
        createdAt = at,
        evaluationType = type,
        answers = answers,
        totalScore = answers.sum()
    )
}
