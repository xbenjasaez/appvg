package cl.ipvg.docentecalma.ui.screens.breathingwithvirgi

object BreathingCopy {
    const val title = "Respira con Virgi"
    const val subtitle = "Una pausa breve para bajar el ritmo."
    const val introSupport = "Solo sigue el movimiento y respira a tu tiempo."
    const val cardSubtitle = "Respiración guiada con apoyo visual."
    const val estimatedDuration = "~1 min"
    const val ctaStart = "Comenzar"
    const val ctaBackIntro = "Volver"
    const val phaseInhale = "Inhala"
    const val phaseHold = "Sostén"
    const val phaseExhale = "Exhala"
    const val phaseRest = "Descansa"
    const val cycleProgress = "Ciclo %1\$d de %2\$d"
    const val closingMain = "Bien. Ya hiciste una pausa para ti."
    const val closingSecondary = "A veces un minuto sí cambia el tono del resto del día."
    const val ctaRepeat = "Hacerlo otra vez"
    const val ctaFinishBack = "Volver al inicio"
    const val ctaExitSession = "Salir"

    fun cycleProgress(current: Int, total: Int): String =
        cycleProgress.format(current, total)

    fun orbContentDescription(phaseLabel: String, secondsRemaining: Int): String =
        "Círculo de respiración, fase: $phaseLabel, $secondsRemaining segundos restantes"

    fun sessionHudContentDescription(
        phaseLabel: String,
        secondsRemaining: Int,
        cycleLabel: String
    ): String = "$phaseLabel, $secondsRemaining segundos, $cycleLabel"
}
