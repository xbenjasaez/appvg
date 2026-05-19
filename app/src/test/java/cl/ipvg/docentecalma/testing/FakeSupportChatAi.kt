package cl.ipvg.docentecalma.testing

import cl.ipvg.docentecalma.ai.AiResult
import cl.ipvg.docentecalma.ai.SupportChatAi
import cl.ipvg.docentecalma.ai.SupportChatTurn

/**
 * Implementación programable de [SupportChatAi] para tests del ViewModel.
 *
 * - [nextResult] define qué devolverá la próxima llamada. Si es null y se
 *   pasó [defaultResult], se usa este último; de lo contrario, una respuesta
 *   de éxito simple "Te escucho.".
 * - Guarda todas las invocaciones en [calls] para aserciones.
 */
class FakeSupportChatAi(
    private val defaultResult: AiResult? = null
) : SupportChatAi {

    data class Call(val history: List<SupportChatTurn>, val userMessage: String)

    private val _calls = mutableListOf<Call>()
    val calls: List<Call> get() = _calls.toList()

    var nextResult: AiResult? = null

    override suspend fun reply(
        history: List<SupportChatTurn>,
        userMessage: String
    ): AiResult {
        _calls += Call(history, userMessage)
        val result = nextResult ?: defaultResult ?: AiResult.Success(text = "Te escucho.")
        nextResult = null
        return result
    }
}
