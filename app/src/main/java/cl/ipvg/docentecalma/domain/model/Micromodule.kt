package cl.ipvg.docentecalma.domain.model

/**
 * Contenido estático de un micromódulo: lectura breve (orientación microlearning).
 *
 * Los textos viven en [cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog].
 * El progreso del usuario se persiste aparte ([MicromoduleUserProgress]).
 */
data class Micromodule(
    val id: String,
    val title: String,
    val lead: String,
    val estimatedMinutes: Int,
    val blocks: List<MicromoduleBlock>,
    /** Opcional: enlaza con un id de [QuickExercise] del catálogo de ejercicios breves. */
    val relatedExerciseId: String? = null
)

/**
 * Fragmento escaneable: título corto + líneas breves (frases o viñetas).
 */
data class MicromoduleBlock(
    val heading: String,
    val lines: List<String>
)
