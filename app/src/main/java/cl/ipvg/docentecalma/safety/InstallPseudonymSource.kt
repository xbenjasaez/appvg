package cl.ipvg.docentecalma.safety

/**
 * Abstracción mínima del seudónimo por instalación.
 * Permite inyectar [RiskEventFactory] en pruebas sin [android.content.Context].
 */
fun interface InstallPseudonymSource {
    fun installPseudonym(): String
}
