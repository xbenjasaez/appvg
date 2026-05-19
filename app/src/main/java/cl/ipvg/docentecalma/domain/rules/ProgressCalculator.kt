package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionCategory
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.RecommendationHistory
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Cálculos agregados para la pantalla de Progreso.
 *
 * Todo es puro: recibe listas y produce estructuras inmutables. Sin Flow,
 * sin Room, sin Compose. Se prueba unitariamente sin dependencias.
 *
 * Todas las ventanas temporales se expresan en días y se calculan con
 * [ChronoUnit.DAYS] sobre instantes UTC provistos por el ViewModel.
 */
object ProgressCalculator {

    /** Agregado de una ventana temporal sobre una lista de chequeos. */
    data class WindowAggregate(
        val windowDays: Int,
        val totalCheckIns: Int,
        val averageIntensity: Double?,
        val countByEmotion: Map<Emotion, Int>,
        val countByCategory: Map<EmotionCategory, Int>,
        val mostFrequentEmotion: Emotion?,
        val mostFrequentCount: Int,
        val difficultShare: Double?,
        val regulatedShare: Double?
    ) {
        companion object {
            fun empty(days: Int): WindowAggregate = WindowAggregate(
                windowDays = days,
                totalCheckIns = 0,
                averageIntensity = null,
                countByEmotion = emptyMap(),
                countByCategory = emptyMap(),
                mostFrequentEmotion = null,
                mostFrequentCount = 0,
                difficultShare = null,
                regulatedShare = null
            )
        }
    }

    /** Resumen de un bucket semanal (los últimos 7 días a partir de [until]). */
    data class WeekBucket(
        val index: Int,
        val startAt: Instant,
        val endAt: Instant,
        val totalCheckIns: Int,
        val averageIntensity: Double?
    )

    /**
     * Filtra [checkIns] a los que ocurrieron en [lastDays] días desde [now].
     * [checkIns] puede venir en cualquier orden.
     */
    fun windowOf(
        checkIns: List<EmotionalCheckIn>,
        lastDays: Int,
        now: Instant
    ): List<EmotionalCheckIn> {
        val cutoff = now.minus(lastDays.toLong(), ChronoUnit.DAYS)
        return checkIns.filter { it.createdAt.isAfter(cutoff) || it.createdAt == cutoff }
    }

    /** Computa el agregado para una ventana ya filtrada. */
    fun aggregate(window: List<EmotionalCheckIn>, windowDays: Int): WindowAggregate {
        if (window.isEmpty()) return WindowAggregate.empty(windowDays)

        val countByEmotion: Map<Emotion, Int> = window.groupingBy { it.emotion }.eachCount()
        val countByCategory: Map<EmotionCategory, Int> = window
            .groupingBy { it.emotion.category }
            .eachCount()
        val mostFrequent = countByEmotion.maxByOrNull { it.value }
        val averageIntensity = window.map { it.intensity }.average()

        val difficult = (countByCategory[EmotionCategory.DIFFICULT_HIGH_ACTIVATION] ?: 0) +
            (countByCategory[EmotionCategory.DIFFICULT_LOW_ENERGY] ?: 0)
        val regulated = countByCategory[EmotionCategory.REGULATED_POSITIVE] ?: 0
        val total = window.size

        return WindowAggregate(
            windowDays = windowDays,
            totalCheckIns = total,
            averageIntensity = averageIntensity,
            countByEmotion = countByEmotion,
            countByCategory = countByCategory,
            mostFrequentEmotion = mostFrequent?.key,
            mostFrequentCount = mostFrequent?.value ?: 0,
            difficultShare = difficult.toDouble() / total,
            regulatedShare = regulated.toDouble() / total
        )
    }

    /**
     * Divide los últimos [bucketCount] * 7 días en buckets semanales. El bucket
     * 0 es el más reciente (últimos 7 días), el 1 los 7 anteriores, etc.
     */
    fun weeklyBuckets(
        checkIns: List<EmotionalCheckIn>,
        bucketCount: Int,
        now: Instant
    ): List<WeekBucket> {
        require(bucketCount > 0) { "bucketCount debe ser > 0" }
        return (0 until bucketCount).map { index ->
            val endAt = now.minus((index * 7).toLong(), ChronoUnit.DAYS)
            val startAt = endAt.minus(7, ChronoUnit.DAYS)
            val inBucket = checkIns.filter {
                (it.createdAt.isAfter(startAt) || it.createdAt == startAt) &&
                    it.createdAt.isBefore(endAt)
            }
            WeekBucket(
                index = index,
                startAt = startAt,
                endAt = endAt,
                totalCheckIns = inBucket.size,
                averageIntensity = if (inBucket.isEmpty()) null
                else inBucket.map { it.intensity }.average()
            )
        }
    }

    /**
     * Eventos para un historial unificado (chequeos, autoevaluaciones, pasos guardados).
     */
    sealed interface PersonalTimelineEntry {
        val occurredAt: Instant

        data class CheckInEntry(val checkIn: EmotionalCheckIn) : PersonalTimelineEntry {
            override val occurredAt: Instant get() = checkIn.createdAt
        }

        data class AssessmentEntry(val assessment: SelfAssessment) : PersonalTimelineEntry {
            override val occurredAt: Instant get() = assessment.createdAt
        }

        data class SavedRecommendationEntry(val history: RecommendationHistory) :
            PersonalTimelineEntry {
            override val occurredAt: Instant get() = history.createdAt
        }
    }

    /**
     * Une y ordena actividad reciente. [maxItems] limita el tamaño para la UI.
     */
    fun mergePersonalTimeline(
        checkIns: List<EmotionalCheckIn>,
        assessments: List<SelfAssessment>,
        recommendations: List<RecommendationHistory>,
        maxItems: Int
    ): List<PersonalTimelineEntry> {
        require(maxItems > 0) { "maxItems debe ser > 0" }
        val merged = buildList {
            checkIns.forEach { add(PersonalTimelineEntry.CheckInEntry(it)) }
            assessments.forEach { add(PersonalTimelineEntry.AssessmentEntry(it)) }
            recommendations.forEach { add(PersonalTimelineEntry.SavedRecommendationEntry(it)) }
        }
        return merged
            .sortedWith(
                compareByDescending<PersonalTimelineEntry> { it.occurredAt }
                    .thenByDescending {
                        when (it) {
                            is PersonalTimelineEntry.CheckInEntry -> 2
                            is PersonalTimelineEntry.AssessmentEntry -> 1
                            is PersonalTimelineEntry.SavedRecommendationEntry -> 0
                        }
                    }
            )
            .take(maxItems)
    }

    /**
     * Días distintos (zona local) con al menos un registro en la ventana inclusive
     * [lastDays] contando desde hoy.
     */
    fun distinctActiveLocalDays(
        instants: List<Instant>,
        zone: ZoneId,
        now: Instant,
        lastDays: Int
    ): Int {
        require(lastDays > 0) { "lastDays debe ser > 0" }
        if (instants.isEmpty()) return 0
        val today = now.atZone(zone).toLocalDate()
        val start = today.minusDays(lastDays.toLong() - 1)
        return instants
            .map { it.atZone(zone).toLocalDate() }
            .filter { !it.isBefore(start) && !it.isAfter(today) }
            .distinct()
            .size
    }

    /**
     * Ideas breves a partir del historial; tono acompañante, sin juicio ni vigilancia.
     * Máximo tres frases.
     */
    fun personalInsightLines(
        activeDaysLast14: Int,
        last7: WindowAggregate,
        checkIns: List<EmotionalCheckIn>,
        assessmentsSortedNewestFirst: List<SelfAssessment>,
        exerciseRecommendationCount: Int,
        now: Instant,
        zone: ZoneId
    ): List<String> {
        val lines = mutableListOf<String>()

        if (assessmentsSortedNewestFirst.size >= 2) {
            val latest = assessmentsSortedNewestFirst[0]
            val previous = assessmentsSortedNewestFirst[1]
            when {
                latest.totalScore < previous.totalScore ->
                    lines.add(
                        "En tu última autoevaluación la carga percibida bajó un poco " +
                            "respecto a la anterior. ¿Notaste algo que te ayudó?"
                    )

                latest.totalScore > previous.totalScore ->
                    lines.add(
                        "Tu última autoevaluación muestra un poco más de carga. " +
                            "Elegir un recurso breve cuando puedas también cuenta como cuidado."
                    )
            }
        }

        val difficult = last7.difficultShare
        val regulated = last7.regulatedShare
        if (last7.totalCheckIns >= 3 && difficult != null && difficult >= 0.55) {
            lines.add(
                "La última semana tuvo emociones exigentes. Un descanso corto o un " +
                    "chequeo breve puede ser suficiente hoy."
            )
        } else if (last7.totalCheckIns >= 3 && regulated != null && regulated >= 0.55) {
            lines.add(
                "En los últimos días hubo momentos más regulados o positivos. " +
                    "Reconocerlos también forma parte del bienestar."
            )
        }

        if (activeDaysLast14 >= 5) {
            lines.add(
                "Llevas varios días en los que la app forma parte de tu rutina. " +
                    "No hace falta ser constante todos los días: lo que importa es volver cuando puedas."
            )
        }

        val lastCheckAt = checkIns.maxByOrNull { it.createdAt }?.createdAt
        val daysSinceCheckIn =
            lastCheckAt?.let { ChronoUnit.DAYS.between(it.atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate()) }
        if (checkIns.isNotEmpty() && daysSinceCheckIn != null && daysSinceCheckIn >= 10L) {
            lines.add(
                "Hace un tiempo que no registras un chequeo. Cuando te sientas lista o listo, " +
                    "aquí está sin prisa."
            )
        }

        if (lines.isEmpty() && exerciseRecommendationCount > 0) {
            lines.add(
                "Tienes pasos guardados desde cuando pediste apoyo en la app. " +
                    "Puedes volver a ellos cuando te hagan sentido."
            )
        }

        if (lines.isEmpty() && checkIns.isNotEmpty()) {
            lines.add(
                "Tu historial cuenta una historia solo tuya. Puedes usarlo para " +
                    "notar qué te acompaña en distintas semanas."
            )
        }

        if (lines.isEmpty() && assessmentsSortedNewestFirst.isNotEmpty()) {
            lines.add(
                "Tus autoevaluaciones quedan reunidas aquí para cuando quieras mirar " +
                    "cómo ha sido tu semana, sin etiquetas ni juicios."
            )
        }

        return lines.distinct().take(3)
    }
}
