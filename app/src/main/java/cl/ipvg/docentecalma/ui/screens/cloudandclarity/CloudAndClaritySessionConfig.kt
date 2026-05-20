package cl.ipvg.docentecalma.ui.screens.cloudandclarity

object CloudAndClaritySessionConfig {
    const val MASK_COLS = 44
    const val MASK_ROWS = 64

    /** Fracción despejada para activar el microcierre (casi 100% real en la malla). */
    const val COMPLETION_THRESHOLD = 0.98f

    /** Valor de celda ≤ esto cuenta como despejada al calcular progreso. */
    const val CELL_CLEARED_EPSILON = 0.08f

    /** Radio del pincel en coordenadas normalizadas (0–1). */
    const val BRUSH_RADIUS_NORM = 0.085f

    /** Distancia mínima entre muestras al interpolar el trazo. */
    const val ERASE_SAMPLE_STEP_NORM = 0.018f

    const val AMBIENT_TICK_MS = 48L
    const val COMPLETE_HOLD_MS = 420L
}
