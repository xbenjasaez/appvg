package cl.ipvg.docentecalma.domain.model

/**
 * Escenario de guía de aula: orientación para situaciones complejas con estudiantes.
 */
data class ClassroomScenario(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<String>,
    val redFlags: List<String>,
    val whenToEscalate: String
)
