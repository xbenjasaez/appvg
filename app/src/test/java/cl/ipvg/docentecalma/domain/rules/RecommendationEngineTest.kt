package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.RecommendationType
import cl.ipvg.docentecalma.domain.model.SeverityFlag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RecommendationEngineTest {

    private val engine = RecommendationEngine()

    // --- contenido base --------------------------------------------------

    @Test
    fun `build compone todos los campos desde el rule catalog`() {
        val rec = engine.build(checkInWith(Emotion.STRESS, intensity = 3))

        val rule = EmotionRuleCatalog.ruleFor(Emotion.STRESS)
        assertEquals(rule.title, rec.title)
        assertEquals(rule.immediateAction, rec.immediateAction)
        assertEquals(rule.breathingSuggestion, rec.breathingSuggestion)
        assertEquals(rule.whatToAvoid, rec.whatToAvoid)
        assertEquals(rule.optionalPedagogicalTip, rec.optionalPedagogicalTip)
        assertEquals(Emotion.STRESS, rec.emotion)
        assertEquals(3, rec.intensity)
    }

    @Test
    fun `build cubre todas las emociones sin fallar`() {
        Emotion.entries.forEach { emotion ->
            val rec = engine.build(checkInWith(emotion, intensity = 3))
            assertTrue("shortMessage vacío para $emotion", rec.shortMessage.isNotBlank())
            assertTrue("title vacío para $emotion", rec.title.isNotBlank())
            assertTrue("immediateAction vacío para $emotion", rec.immediateAction.isNotBlank())
            assertTrue("breathingSuggestion vacío para $emotion", rec.breathingSuggestion.isNotBlank())
            assertTrue("whatToAvoid vacío para $emotion", rec.whatToAvoid.isNotBlank())
        }
    }

    // --- severity --------------------------------------------------------

    @Test
    fun `severity es NORMAL para emociones reguladas en cualquier intensidad`() {
        listOf(Emotion.CALM, Emotion.HAPPY).forEach { emotion ->
            (1..5).forEach { intensity ->
                val rec = engine.build(checkInWith(emotion, intensity))
                assertEquals(
                    "Regulada $emotion intensidad $intensity debe ser NORMAL",
                    SeverityFlag.NORMAL,
                    rec.severity
                )
            }
        }
    }

    @Test
    fun `severity escala con intensidad para emociones dificiles`() {
        assertEquals(SeverityFlag.NORMAL, engine.build(checkInWith(Emotion.STRESS, 1)).severity)
        assertEquals(SeverityFlag.NORMAL, engine.build(checkInWith(Emotion.STRESS, 2)).severity)
        assertEquals(SeverityFlag.SUGGEST_CHAT, engine.build(checkInWith(Emotion.STRESS, 3)).severity)
        assertEquals(
            SeverityFlag.SUGGEST_PROFESSIONAL,
            engine.build(checkInWith(Emotion.STRESS, 4)).severity
        )
        assertEquals(
            SeverityFlag.SUGGEST_PROFESSIONAL,
            engine.build(checkInWith(Emotion.STRESS, 5)).severity
        )
    }

    // --- suggestedExercise ----------------------------------------------

    @Test
    fun `emociones con ejercicio sugerido lo incluyen en el resultado`() {
        val rec = engine.build(checkInWith(Emotion.STRESS, intensity = 2))
        assertNotNull("Debe sugerir un ejercicio para STRESS", rec.suggestedExercise)
        assertEquals(
            EmotionRuleCatalog.ruleFor(Emotion.STRESS).suggestedExerciseId,
            rec.suggestedExercise?.id
        )
    }

    @Test
    fun `emociones reguladas no sugieren ejercicio`() {
        assertNull(engine.build(checkInWith(Emotion.CALM, 3)).suggestedExercise)
        assertNull(engine.build(checkInWith(Emotion.HAPPY, 5)).suggestedExercise)
    }

    // --- suggestedMicromoduleId -----------------------------------------

    @Test
    fun `suggestedMicromoduleId apunta a un id del catalogo`() {
        Emotion.entries.forEach { emotion ->
            val id = requireNotNull(engine.build(checkInWith(emotion, 3)).suggestedMicromoduleId)
            assertEquals(MicromoduleCatalog.suggestedIdFor(emotion), id)
            assertNotNull(MicromoduleCatalog.byIdOrNull(id))
        }
    }

    // --- toHistoryType ---------------------------------------------------

    @Test
    fun `toHistoryType prioriza PROFESSIONAL sobre ejercicio`() {
        val rec = engine.build(checkInWith(Emotion.STRESS, 5))
        assertEquals(RecommendationType.PROFESSIONAL, rec.toHistoryType())
    }

    @Test
    fun `toHistoryType retorna CHAT para intensidad 3 en emociones dificiles`() {
        val rec = engine.build(checkInWith(Emotion.SADNESS, 3))
        assertEquals(RecommendationType.CHAT, rec.toHistoryType())
    }

    @Test
    fun `toHistoryType retorna EXERCISE cuando no escala pero hay ejercicio`() {
        val rec = engine.build(checkInWith(Emotion.STRESS, 2))
        assertEquals(RecommendationType.EXERCISE, rec.toHistoryType())
    }

    @Test
    fun `toHistoryType retorna IMMEDIATE para reguladas sin ejercicio`() {
        val rec = engine.build(checkInWith(Emotion.CALM, 3))
        assertEquals(RecommendationType.IMMEDIATE, rec.toHistoryType())
    }

    // --- shortMessage ----------------------------------------------------

    @Test
    fun `shortMessage contiene el nombre de la emocion en minusculas`() {
        val rec = engine.build(checkInWith(Emotion.FRUSTRATION, 4))
        assertTrue(
            "shortMessage debe mencionar 'frustración' -> ${rec.shortMessage}",
            rec.shortMessage.contains("frustración")
        )
    }

    @Test
    fun `shortMessage de reguladas no contiene la palabra trabajo ni profesional`() {
        val rec = engine.build(checkInWith(Emotion.CALM, 5))
        assertFalse(rec.shortMessage.contains("trabajo"))
        assertFalse(rec.shortMessage.contains("profesional"))
    }

    // --- helpers ---------------------------------------------------------

    private fun checkInWith(
        emotion: Emotion,
        intensity: Int,
        note: String? = null
    ): EmotionalCheckIn = EmotionalCheckIn(
        id = 1L,
        emotion = emotion,
        intensity = intensity,
        note = note,
        createdAt = Instant.ofEpochMilli(1_700_000_000_000L)
    )
}
