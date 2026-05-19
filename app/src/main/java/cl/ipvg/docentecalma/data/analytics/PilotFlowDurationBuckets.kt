package cl.ipvg.docentecalma.data.analytics

/**
 * Buckets de duración en pantalla (segundos totales aproximados), sin registro continuo ni timestamps finos.
 *
 * 0 = menos de 30 s; 1 = 30 s a 2 min; 2 = 2 a 10 min; 3 = 10 a 30 min; 4 = más de 30 min.
 */
object PilotFlowDurationBuckets {
    const val UNDER_30S: Int = 0
    const val SEC_30_TO_2MIN: Int = 1
    const val MIN_2_TO_10: Int = 2
    const val MIN_10_TO_30: Int = 3
    const val OVER_30MIN: Int = 4

    fun bucket(totalSeconds: Int): Int = when {
        totalSeconds < 0 -> UNDER_30S
        totalSeconds < 30 -> UNDER_30S
        totalSeconds < 120 -> SEC_30_TO_2MIN
        totalSeconds < 600 -> MIN_2_TO_10
        totalSeconds < 1800 -> MIN_10_TO_30
        else -> OVER_30MIN
    }
}
