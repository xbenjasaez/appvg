package cl.ipvg.docentecalma.ui.screens.traceandrelease

object TraceAndReleaseCopy {
    const val title = "Camino de Virgi"
    const val subtitle = "Traza un camino suave y deja que Virgi lo recorra contigo."
    const val introHelp = "Haz tres trazos lentos. No importa cómo se vean."
    const val ctaStart = "Comenzar"
    const val ctaBack = "Volver"
    const val ctaClear = "Limpiar"
    const val canvasHint = "Sigue a tu ritmo."
    const val strokeProgress = "Trazo %1\$d de %2\$d"
    const val closingMain = "Bien. Ya hiciste una pausa con movimiento."
    const val closingSecondary = "A veces seguir un trazo simple ayuda más de lo que parece."
    const val ctaContinue = "Seguir un poco más"
    const val ctaFinishBack = "Volver al inicio"
    const val cardSubtitle = "Traza un camino suave y deja que Virgi te acompañe."
    const val estimatedDuration = "30–90 s"

    fun strokeProgress(current: Int, total: Int): String =
        strokeProgress.format(current, total)
}
