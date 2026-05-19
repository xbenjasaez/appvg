package cl.ipvg.docentecalma.domain.model

/**
 * Tipo de registro de la autoevaluación breve (autoinforme, no clínico).
 */
enum class SelfAssessmentEvaluationType(val storageId: String) {
    INITIAL("INITIAL"),
    PERIODIC("PERIODIC");

    companion object {
        fun fromStorageId(id: String): SelfAssessmentEvaluationType =
            entries.firstOrNull { it.storageId == id } ?: PERIODIC
    }
}
