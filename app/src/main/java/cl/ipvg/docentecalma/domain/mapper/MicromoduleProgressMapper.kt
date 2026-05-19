package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.MicromoduleProgressEntity
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.model.MicromoduleUserProgress
import java.time.Instant

fun MicromoduleProgressEntity.toDomain(): MicromoduleUserProgress = MicromoduleUserProgress(
    moduleId = moduleId,
    state = MicromoduleProgressState.valueOf(state),
    lastOpenedAt = lastOpenedAt?.let(Instant::ofEpochMilli),
    startedAt = startedAt?.let(Instant::ofEpochMilli),
    completedAt = completedAt?.let(Instant::ofEpochMilli)
)

fun MicromoduleUserProgress.toEntity(): MicromoduleProgressEntity = MicromoduleProgressEntity(
    moduleId = moduleId,
    state = state.name,
    lastOpenedAt = lastOpenedAt?.toEpochMilli(),
    startedAt = startedAt?.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli()
)
