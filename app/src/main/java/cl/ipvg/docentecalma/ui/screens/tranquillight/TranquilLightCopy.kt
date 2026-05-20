package cl.ipvg.docentecalma.ui.screens.tranquillight

object TranquilLightCopy {
    const val title = "Luz tranquila"
    const val subtitle = "Toca las luces suaves y ayúdalas a volver al farol."
    const val introHelp = "No necesitas apurarte. Solo sigue una luz a la vez."
    const val ctaStart = "Comenzar"
    const val ctaBack = "Volver"
    const val playingHint = "Sigue a tu ritmo."
    const val lightProgress = "%d de %d luces"
    const val closingMain = "Bien. Ya reuniste un poco de calma."
    const val closingSecondary = "A veces enfocarse en una sola luz basta por un momento."
    const val ctaRepeat = "Hacerlo otra vez"
    const val ctaFinishBack = "Volver al inicio"
    const val cardSubtitle = "Toca luces suaves y reúnelas en el farol."
    const val estimatedDuration = "1–2 min"

    fun lightProgress(collected: Int, total: Int): String =
        lightProgress.format(collected, total)

    fun lightContentDescription(): String = "Luz suave, tocar para reunir"

    fun playfieldContentDescription(collected: Int, total: Int): String =
        lightProgress(collected, total)
}
