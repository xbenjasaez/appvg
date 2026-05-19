package cl.ipvg.docentecalma.domain.model

/**
 * Ejercicio breve de regulación (respiración, grounding, pausa activa, etc.).
 */
data class QuickExercise(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val steps: List<String>
)
