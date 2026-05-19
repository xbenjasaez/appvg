package cl.ipvg.docentecalma.domain.mapper

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmotionLabelsTest {

    @Test
    fun `displayName cubre todas las emociones del enum`() {
        Emotion.entries.forEach { emotion ->
            val label = EmotionLabels.displayName(emotion)
            assertTrue("displayName para $emotion debe ser no vacío", label.isNotBlank())
        }
    }

    @Test
    fun `shortDescription cubre todas las emociones del enum`() {
        Emotion.entries.forEach { emotion ->
            val desc = EmotionLabels.shortDescription(emotion)
            assertTrue("shortDescription para $emotion debe ser no vacío", desc.isNotBlank())
        }
    }

    @Test
    fun `categoryLabel cubre todas las categorias del enum`() {
        EmotionCategory.entries.forEach { category ->
            val label = EmotionLabels.categoryLabel(category)
            assertTrue("categoryLabel para $category debe ser no vacío", label.isNotBlank())
        }
    }

    @Test
    fun `intensityLabel retorna etiquetas en orden creciente para valores validos`() {
        assertEquals("Muy leve", EmotionLabels.intensityLabel(1))
        assertEquals("Leve", EmotionLabels.intensityLabel(2))
        assertEquals("Moderada", EmotionLabels.intensityLabel(3))
        assertEquals("Intensa", EmotionLabels.intensityLabel(4))
        assertEquals("Muy intensa", EmotionLabels.intensityLabel(5))
    }

    @Test
    fun `intensityLabel aplica coerceIn para valores fuera de rango`() {
        assertEquals("Muy leve", EmotionLabels.intensityLabel(-10))
        assertEquals("Muy leve", EmotionLabels.intensityLabel(0))
        assertEquals("Muy intensa", EmotionLabels.intensityLabel(6))
        assertEquals("Muy intensa", EmotionLabels.intensityLabel(999))
    }

    @Test
    fun `extensiones mapean al mismo valor que las funciones`() {
        val sample = Emotion.STRESS
        assertEquals(EmotionLabels.displayName(sample), sample.displayName)
        assertEquals(EmotionLabels.shortDescription(sample), sample.shortDescription)
        assertEquals(
            EmotionLabels.categoryLabel(sample.category),
            sample.category.label
        )
    }
}
