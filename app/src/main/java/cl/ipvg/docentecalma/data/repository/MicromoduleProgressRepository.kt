package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.dao.MicromoduleProgressDao
import cl.ipvg.docentecalma.data.local.entity.MicromoduleProgressEntity
import cl.ipvg.docentecalma.domain.mapper.toDomain
import cl.ipvg.docentecalma.domain.model.MicromoduleProgressState
import cl.ipvg.docentecalma.domain.model.MicromoduleUserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MicromoduleProgressRepository @Inject constructor(
    private val dao: MicromoduleProgressDao
) {

    fun observeAll(): Flow<List<MicromoduleUserProgress>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun get(moduleId: String): MicromoduleUserProgress? =
        dao.getByModuleId(moduleId)?.toDomain()

    /**
     * Primera apertura: crea fila en progreso. Reaperturas: actualiza [MicromoduleUserProgress.lastOpenedAt].
     * Si ya estaba completado, no revierte el estado.
     */
    /**
     * @return `true` si es la primera vez que se abre este micromódulo en este dispositivo
     * (fila nueva); `false` si ya existía registro.
     */
    suspend fun markOpened(moduleId: String, now: Instant = Instant.now()): Boolean {
        val epoch = now.toEpochMilli()
        val existing = dao.getByModuleId(moduleId)
        val isFirstEverOpen = existing == null
        when {
            existing == null -> {
                dao.upsert(
                    MicromoduleProgressEntity(
                        moduleId = moduleId,
                        state = MicromoduleProgressState.IN_PROGRESS.name,
                        lastOpenedAt = epoch,
                        startedAt = epoch,
                        completedAt = null
                    )
                )
            }

            existing.state == MicromoduleProgressState.COMPLETED.name -> {
                dao.upsert(existing.copy(lastOpenedAt = epoch))
            }

            else -> {
                dao.upsert(
                    existing.copy(
                        state = MicromoduleProgressState.IN_PROGRESS.name,
                        lastOpenedAt = epoch,
                        startedAt = existing.startedAt ?: epoch
                    )
                )
            }
        }
        return isFirstEverOpen
    }

    suspend fun markCompleted(moduleId: String, now: Instant = Instant.now()) {
        val epoch = now.toEpochMilli()
        val existing = dao.getByModuleId(moduleId)
        if (existing == null) {
            dao.upsert(
                MicromoduleProgressEntity(
                    moduleId = moduleId,
                    state = MicromoduleProgressState.COMPLETED.name,
                    lastOpenedAt = epoch,
                    startedAt = epoch,
                    completedAt = epoch
                )
            )
        } else {
            dao.upsert(
                existing.copy(
                    state = MicromoduleProgressState.COMPLETED.name,
                    lastOpenedAt = epoch,
                    startedAt = existing.startedAt ?: epoch,
                    completedAt = epoch
                )
            )
        }
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
