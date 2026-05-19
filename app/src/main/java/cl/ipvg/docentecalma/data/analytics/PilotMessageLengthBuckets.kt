package cl.ipvg.docentecalma.data.analytics

/**
 * Buckets discretos para longitud de mensaje (sin almacenar el texto).
 * 0 = vacío, 1 = corto, 2 = medio, 3 = largo.
 */
object PilotMessageLengthBuckets {
    const val EMPTY: Int = 0
    const val SHORT: Int = 1
    const val MEDIUM: Int = 2
    const val LONG: Int = 3

    fun bucket(charCount: Int): Int = when {
        charCount <= 0 -> EMPTY
        charCount < 50 -> SHORT
        charCount < 200 -> MEDIUM
        else -> LONG
    }
}
