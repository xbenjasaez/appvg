package cl.ipvg.docentecalma.ui.screens.cloudandclarity

object CloudAndClarityCopy {
    const val title = "Nube y claridad"
    const val subtitle = "Despeja la pantalla lentamente y deja que aparezca un poco de calma."
    const val introHelp = "No hace falta apurarse. Solo abre espacio con el gesto."
    const val ctaStart = "Comenzar"
    const val ctaBack = "Volver"
    const val playingHint = "Sigue a tu ritmo."
    const val clearedProgress = "%d%% despejado"
    const val closingMain = "Bien. Ya abriste un poco más de espacio."
    const val closingSecondary =
        "A veces despejar un poco también cambia cómo se siente el resto."
    const val ctaRepeat = "Hacerlo otra vez"
    const val ctaFinishBack = "Volver al inicio"
    const val cardSubtitle = "Despeja la pantalla y deja aparecer un poco de calma."
    const val estimatedDuration = "2–3 min"

    fun clearedProgress(percent: Int): String = clearedProgress.format(percent)

    fun playfieldContentDescription(percent: Int): String = clearedProgress(percent)
}
