package cl.ipvg.docentecalma.data.repository

import cl.ipvg.docentecalma.data.local.dao.SelfAssessmentDao
import cl.ipvg.docentecalma.data.local.entity.SelfAssessmentEntity
import cl.ipvg.docentecalma.domain.mapper.toDomain
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelfAssessmentRepository @Inject constructor(
    private val dao: SelfAssessmentDao
) {

    fun observeAll(): Flow<List<SelfAssessment>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getLatest(): SelfAssessment? = dao.getLatest()?.toDomain()

    suspend fun getById(id: Long): SelfAssessment? = dao.getById(id)?.toDomain()

    /**
     * Guarda respuestas (4 valores 1–5), asigna tipo inicial o periódico y devuelve el id.
     */
    suspend fun saveAnswers(
        answers: List<Int>,
        createdAt: Instant = Instant.now()
    ): Long {
        require(answers.size == SelfAssessment.QUESTION_COUNT) {
            "Se esperaban ${SelfAssessment.QUESTION_COUNT} respuestas."
        }
        require(answers.all { it in SelfAssessment.SCORE_RANGE }) {
            "Cada respuesta debe estar en ${SelfAssessment.SCORE_RANGE}."
        }
        val total = answers.sum()
        val type = if (dao.count() == 0L) {
            SelfAssessmentEvaluationType.INITIAL
        } else {
            SelfAssessmentEvaluationType.PERIODIC
        }
        val entity = SelfAssessmentEntity(
            createdAt = createdAt.toEpochMilli(),
            evaluationType = type.storageId,
            q1 = answers[0],
            q2 = answers[1],
            q3 = answers[2],
            q4 = answers[3],
            totalScore = total
        )
        return dao.insert(entity)
    }

    suspend fun deleteAll() = dao.deleteAll()
}
