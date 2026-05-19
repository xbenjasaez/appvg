package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.data.local.entity.SelfAssessmentEntity
import cl.ipvg.docentecalma.domain.model.SelfAssessment
import cl.ipvg.docentecalma.domain.model.SelfAssessmentEvaluationType
import java.time.Instant

fun SelfAssessmentEntity.toDomain(): SelfAssessment = SelfAssessment(
    id = id,
    createdAt = Instant.ofEpochMilli(createdAt),
    evaluationType = SelfAssessmentEvaluationType.fromStorageId(evaluationType),
    answers = listOf(q1, q2, q3, q4),
    totalScore = totalScore
)
