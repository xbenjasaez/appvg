package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.domain.model.ClassroomScenario
import cl.ipvg.docentecalma.domain.rules.ClassroomScenarioCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de escenarios de aula. Hoy usa un catálogo estático; si en el futuro
 * se edita desde la app, se puede reemplazar por un DAO sin cambiar los consumidores.
 */
@Singleton
class ClassroomGuidanceRepository @Inject constructor() {

    fun observeScenarios(): Flow<List<ClassroomScenario>> = flowOf(ClassroomScenarioCatalog.all)

    fun getScenario(id: String): ClassroomScenario? = ClassroomScenarioCatalog.byId(id)
}
