package cl.ipvg.docentecalma.domain.model

import java.time.Instant

/**
 * Progreso persistido de un micromódulo (metadatos de uso; el contenido es [Micromodule]).
 */
data class MicromoduleUserProgress(
    val moduleId: String,
    val state: MicromoduleProgressState,
    val lastOpenedAt: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?
)
