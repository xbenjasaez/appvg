package cl.ipvg.docentecalma.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskClassifierTest {

    @Test
    fun `texto vacio o en blanco se clasifica como NONE`() {
        assertEquals(RiskCategory.NONE, RiskClassifier.categorize(""))
        assertEquals(RiskCategory.NONE, RiskClassifier.categorize("   "))
    }

    @Test
    fun `mensaje cotidiano no gatilla ninguna alerta`() {
        val samples = listOf(
            "Hola, ¿cómo estás?",
            "Hoy tuve una clase difícil pero ya se pasó.",
            "Quiero organizar mi próxima semana."
        )
        samples.forEach {
            assertEquals(
                "No debería alertar: $it",
                RiskCategory.NONE,
                RiskClassifier.categorize(it)
            )
        }
    }

    @Test
    fun `menciones de autolesion o suicidio caen en SELF_HARM`() {
        val samples = listOf(
            "A veces pienso en suicidarme",
            "Tengo ganas de hacerme daño",
            "No quiero seguir viviendo",
            "Estoy pensando en cortarme"
        )
        samples.forEach {
            assertEquals(
                "Debería ser SELF_HARM: $it",
                RiskCategory.SELF_HARM,
                RiskClassifier.categorize(it)
            )
        }
    }

    @Test
    fun `impulsos contra otros caen en HARM_TO_OTHERS`() {
        val samples = listOf(
            "Tengo ganas de pegarle a un estudiante",
            "Voy a golpear a alguien si sigue así",
            "Quiero hacerle daño a mi jefe"
        )
        samples.forEach {
            assertEquals(
                "Debería ser HARM_TO_OTHERS: $it",
                RiskCategory.HARM_TO_OTHERS,
                RiskClassifier.categorize(it)
            )
        }
    }

    @Test
    fun `violencia recibida cae en ABUSE_OR_VIOLENCE`() {
        val samples = listOf(
            "Mi pareja me pega",
            "Hay violencia intrafamiliar en mi casa",
            "Alguien abusa de mí"
        )
        samples.forEach {
            assertEquals(
                "Debería ser ABUSE_OR_VIOLENCE: $it",
                RiskCategory.ABUSE_OR_VIOLENCE,
                RiskClassifier.categorize(it)
            )
        }
    }

    @Test
    fun `crisis aguda cae en ACUTE_CRISIS`() {
        val samples = listOf(
            "Creo que tengo una crisis de pánico",
            "Me voy a desmayar",
            "Estoy colapsando, no puedo respirar"
        )
        samples.forEach {
            assertEquals(
                "Debería ser ACUTE_CRISIS: $it",
                RiskCategory.ACUTE_CRISIS,
                RiskClassifier.categorize(it)
            )
        }
    }

    @Test
    fun `SELF_HARM tiene prioridad sobre otras categorias`() {
        val text = "Tengo una crisis de pánico y pienso en quitarme la vida"
        assertEquals(RiskCategory.SELF_HARM, RiskClassifier.categorize(text))
    }

    @Test
    fun `HARM_TO_OTHERS tiene prioridad sobre crisis aguda`() {
        val text = "Estoy colapsando y quiero matar a alguien"
        assertEquals(RiskCategory.HARM_TO_OTHERS, RiskClassifier.categorize(text))
    }

    @Test
    fun `isBlocking es true para cualquier categoria distinta de NONE`() {
        assertFalse(RiskClassifier.isBlocking("Hoy estoy bien"))
        assertTrue(RiskClassifier.isBlocking("Quiero quitarme la vida"))
        assertTrue(RiskClassifier.isBlocking("me pega"))
    }

    @Test
    fun `la respuesta de SELF_HARM menciona Salud Responde y numero`() {
        val msg = RiskClassifier.safetyResponseFor(RiskCategory.SELF_HARM)
        assertTrue(msg.contains("Salud Responde"))
        assertTrue(msg.contains("600 360 7777"))
    }

    @Test
    fun `la respuesta de ABUSE_OR_VIOLENCE menciona 1455 y 133`() {
        val msg = RiskClassifier.safetyResponseFor(RiskCategory.ABUSE_OR_VIOLENCE)
        assertTrue(msg.contains("1455"))
        assertTrue(msg.contains("133"))
    }

    @Test
    fun `la respuesta de ACUTE_CRISIS incluye pauta de respiracion simple`() {
        val msg = RiskClassifier.safetyResponseFor(RiskCategory.ACUTE_CRISIS)
        assertTrue(msg.lowercase().contains("respira"))
    }

    @Test
    fun `ninguna respuesta de seguridad esta vacia`() {
        RiskCategory.entries.forEach { category ->
            val msg = RiskClassifier.safetyResponseFor(category)
            assertTrue("Respuesta vacía para $category", msg.isNotBlank())
        }
    }
}
