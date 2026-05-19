package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MicromoduleCatalogTest {

    @Test
    fun `all tiene lecturas entre 3 y 5 minutos`() {
        MicromoduleCatalog.all.forEach { m ->
            assertTrue(
                "${m.id} fuera de rango",
                m.estimatedMinutes in 3..5
            )
        }
    }

    @Test
    fun `suggestedIdFor coincide con emociones esperadas`() {
        assertEquals("micro_pausa_cognitiva", MicromoduleCatalog.suggestedIdFor(Emotion.STRESS))
        assertEquals("micro_limite_respetuoso", MicromoduleCatalog.suggestedIdFor(Emotion.ANGER))
        assertEquals("micro_cierre_de_jornada", MicromoduleCatalog.suggestedIdFor(Emotion.FATIGUE))
        assertEquals("micro_apertura_de_clase", MicromoduleCatalog.suggestedIdFor(Emotion.CALM))
    }
}
