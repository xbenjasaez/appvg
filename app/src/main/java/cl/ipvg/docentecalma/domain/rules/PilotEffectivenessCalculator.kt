package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.model.MicromoduleUserProgress
import cl.ipvg.docentecalma.domain.model.PilotEffectivenessVerdict
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import kotlin.math.roundToInt

/**
 * Agrega señales locales (autoinforme, micromódulos, continuidad, encuesta breve) en una lectura
 * explícita para la persona usuaria. Toda la lógica es determinista y auditable.
 */
object PilotEffectivenessCalculator {

    data class DimensionDelta(
        val index: Int,
        val baselineAnswer: Int,
        val latestAnswer: Int
    ) {
        /** Negativo o cero favorable: menor frecuencia de carga/tensión percibida. */
        val delta: Int get() = latestAnswer - baselineAnswer
    }

    data class PilotEffectivenessSnapshot(
        val verdict: PilotEffectivenessVerdict,
        val baseline: SelfAssessment?,
        val latest: SelfAssessment?,
        /** Reducción relativa de la suma (0–100). Positivo = menos carga/tensión autoinformada. */
        val improvementPercentOfBaseline: Int?,
        val meaningfulImprovement: Boolean,
        val perDimension: List<DimensionDelta>,
        val activeDistinctDaysLast14: Int,
        val distinctPilotDaysAllTime: Int,
        val totalPilotEvents: Long,
        val modulesCompleted: Int,
        val modulesTotal: Int,
        val moduleCompletionPercent: Int,
        val feedbackPresent: Boolean,
        val satisfaction: Int?,
        val usefulness: Int?,
        val ease: Int?,
        val highSatisfactionAndUsefulness: Boolean,
        val sustainedRecentUse: Boolean
    )

    fun build(
        assessments: List<SelfAssessment>,
        moduleProgress: List<MicromoduleUserProgress>,
        modulesTotal: Int,
        activeDistinctDaysLast14: Int,
        distinctPilotDaysAllTime: Int,
        totalPilotEvents: Long,
        feedbackSatisfaction: Int?,
        feedbackUsefulness: Int?,
        feedbackEase: Int?
    ): PilotEffectivenessSnapshot {
        val sorted = assessments.sortedBy { it.createdAt }
        val baseline = sorted.firstOrNull()
        val latest = sorted.lastOrNull()
        val singleAssessment = sorted.size == 1
        val hasPair = sorted.size >= 2 && baseline != null && latest != null && baseline.id != latest.id

        val modulesCompleted = moduleProgress.count { it.state == MicromoduleProgressState.COMPLETED }
        val modulePct = if (modulesTotal > 0) {
            ((100.0 * modulesCompleted) / modulesTotal).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        val feedbackPresent = feedbackSatisfaction != null &&
            feedbackUsefulness != null &&
            feedbackEase != null
        val highSat = feedbackPresent &&
            feedbackSatisfaction >= HIGH_RATING &&
            feedbackUsefulness >= HIGH_RATING
        val sustainedRecentUse = activeDistinctDaysLast14 >= SUSTAINED_ACTIVE_DAYS_14

        val improvementPct: Int?
        val meaningful: Boolean
        val dimensions: List<DimensionDelta>

        if (hasPair) {
            val b = requireNotNull(baseline)
            val l = requireNotNull(latest)
            val deltaSum = b.totalScore - l.totalScore
            improvementPct = if (b.totalScore > 0) {
                (100.0 * deltaSum / b.totalScore).roundToInt()
            } else {
                null
            }
            meaningful = isMeaningfulScoreImprovement(b.totalScore, l.totalScore)
            dimensions = b.answers.mapIndexed { idx, ansB ->
                val ansL = l.answers[idx]
                DimensionDelta(index = idx, baselineAnswer = ansB, latestAnswer = ansL)
            }
        } else {
            improvementPct = null
            meaningful = false
            dimensions = emptyList()
        }

        val verdict = resolveVerdict(
            assessmentCount = sorted.size,
            hasPair = hasPair,
            meaningfulImprovement = meaningful,
            highSatisfactionAndUsefulness = highSat,
            sustainedRecentUse = sustainedRecentUse,
            feedbackPresent = feedbackPresent,
            activeDistinctDaysLast14 = activeDistinctDaysLast14,
            distinctPilotDaysAllTime = distinctPilotDaysAllTime,
            totalPilotEvents = totalPilotEvents,
            modulesCompleted = modulesCompleted,
            baseline = baseline,
            latest = latest
        )

        return PilotEffectivenessSnapshot(
            verdict = verdict,
            baseline = baseline,
            latest = if (singleAssessment) baseline else latest,
            improvementPercentOfBaseline = improvementPct,
            meaningfulImprovement = meaningful,
            perDimension = dimensions,
            activeDistinctDaysLast14 = activeDistinctDaysLast14,
            distinctPilotDaysAllTime = distinctPilotDaysAllTime,
            totalPilotEvents = totalPilotEvents,
            modulesCompleted = modulesCompleted,
            modulesTotal = modulesTotal,
            moduleCompletionPercent = modulePct,
            feedbackPresent = feedbackPresent,
            satisfaction = feedbackSatisfaction,
            usefulness = feedbackUsefulness,
            ease = feedbackEase,
            highSatisfactionAndUsefulness = highSat,
            sustainedRecentUse = sustainedRecentUse
        )
    }

    private fun isMeaningfulScoreImprovement(baselineTotal: Int, latestTotal: Int): Boolean {
        val gain = baselineTotal - latestTotal
        if (gain <= 0) return false
        val pct = if (baselineTotal > 0) 100.0 * gain / baselineTotal else 0.0
        return gain >= MEANINGFUL_ABS_POINTS || pct >= MEANINGFUL_RELATIVE_PCT
    }

    private fun resolveVerdict(
        assessmentCount: Int,
        hasPair: Boolean,
        meaningfulImprovement: Boolean,
        highSatisfactionAndUsefulness: Boolean,
        sustainedRecentUse: Boolean,
        feedbackPresent: Boolean,
        activeDistinctDaysLast14: Int,
        distinctPilotDaysAllTime: Int,
        totalPilotEvents: Long,
        modulesCompleted: Int,
        baseline: SelfAssessment?,
        latest: SelfAssessment?
    ): PilotEffectivenessVerdict {
        val sparseSignals = isSparseSignals(
            assessmentCount = assessmentCount,
            activeDistinctDaysLast14 = activeDistinctDaysLast14,
            distinctPilotDaysAllTime = distinctPilotDaysAllTime,
            totalPilotEvents = totalPilotEvents,
            modulesCompleted = modulesCompleted,
            feedbackPresent = feedbackPresent
        )

        if (sparseSignals) {
            return PilotEffectivenessVerdict.INSUFFICIENT_EVIDENCE
        }

        if (assessmentCount == 0) {
            return PilotEffectivenessVerdict.USAGE_WITHOUT_SELF_ASSESSMENT_TREND
        }

        if (assessmentCount == 1) {
            return PilotEffectivenessVerdict.AWAITING_SECOND_SELF_ASSESSMENT
        }

        if (!hasPair || baseline == null || latest == null) {
            return PilotEffectivenessVerdict.INSUFFICIENT_EVIDENCE
        }

        if (meaningfulImprovement && highSatisfactionAndUsefulness && sustainedRecentUse) {
            return PilotEffectivenessVerdict.INITIAL_POSITIVE_SIGNALS
        }

        if (meaningfulImprovement && feedbackPresent && !highSatisfactionAndUsefulness) {
            return PilotEffectivenessVerdict.MIXED_SIGNALS
        }

        if (meaningfulImprovement && !feedbackPresent) {
            return PilotEffectivenessVerdict.POSITIVE_TREND_INCOMPLETE_PICTURE
        }

        if (!meaningfulImprovement && sustainedRecentUse) {
            return PilotEffectivenessVerdict.USAGE_WITHOUT_DEMONSTRATED_SHIFT
        }

        return PilotEffectivenessVerdict.SMALL_OR_UNCLEAR_CHANGE
    }

    private fun isSparseSignals(
        assessmentCount: Int,
        activeDistinctDaysLast14: Int,
        distinctPilotDaysAllTime: Int,
        totalPilotEvents: Long,
        modulesCompleted: Int,
        feedbackPresent: Boolean
    ): Boolean {
        val anyEngagement = assessmentCount > 0 ||
            modulesCompleted > 0 ||
            feedbackPresent ||
            activeDistinctDaysLast14 >= 2 ||
            distinctPilotDaysAllTime >= 2 ||
            totalPilotEvents >= MIN_EVENTS_FOR_NON_SPARSE
        return !anyEngagement
    }

    private const val MEANINGFUL_RELATIVE_PCT: Double = 10.0
    private const val MEANINGFUL_ABS_POINTS: Int = 2
    private const val HIGH_RATING: Int = 4
    private const val SUSTAINED_ACTIVE_DAYS_14: Int = 4
    private const val MIN_EVENTS_FOR_NON_SPARSE: Long = 8L
}
