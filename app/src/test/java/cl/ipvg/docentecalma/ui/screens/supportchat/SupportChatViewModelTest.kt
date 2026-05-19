package cl.ipvg.docentecalma.ui.screens.supportchat

import cl.ipvg.docentecalma.MainDispatcherRule
import cl.ipvg.docentecalma.ai.AiResult
import cl.ipvg.docentecalma.data.repository.ChatRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import cl.ipvg.docentecalma.domain.model.ChatRole
import cl.ipvg.docentecalma.safety.InstallPseudonymSource
import cl.ipvg.docentecalma.safety.RiskEvent
import cl.ipvg.docentecalma.safety.RiskEventFactory
import cl.ipvg.docentecalma.safety.RiskEventSink
import cl.ipvg.docentecalma.testing.FakeChatMessageDao
import cl.ipvg.docentecalma.testing.FakePilotAnalyticsEventDao
import cl.ipvg.docentecalma.testing.FakeSupportChatAi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupportChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class RecordingRiskEventSink : RiskEventSink {
        val events = mutableListOf<RiskEvent>()
        override suspend fun emit(event: RiskEvent) {
            events.add(event)
        }
    }

    private fun build(
        ai: FakeSupportChatAi = FakeSupportChatAi()
    ): Env {
        val dao = FakeChatMessageDao()
        val repo = ChatRepository(dao)
        val riskSink = RecordingRiskEventSink()
        val riskFactory = RiskEventFactory(InstallPseudonymSource { "test-install" })
        val pilotRepo = PilotAnalyticsRepository(FakePilotAnalyticsEventDao())
        val vm = SupportChatViewModel(repo, ai, riskFactory, riskSink, pilotRepo)
        return Env(vm, dao, ai, pilotRepo)
    }

    /**
     * Colecta [SupportChatViewModel.uiState] en `backgroundScope` para mantener
     * activa la suscripción de `stateIn(WhileSubscribed)` durante el test.
     */
    private fun TestScope.keepStateSubscribed(vm: SupportChatViewModel): Job =
        backgroundScope.launch { vm.uiState.collect { } }

    @Test
    fun `estado inicial crea sessionId y marca inicializacion completa`() = runTest {
        val env = build()
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        val state = env.vm.uiState.value
        assertTrue(state.sessionId.isNotBlank())
        assertFalse(state.isInitializing)
        assertFalse(state.isGenerating)
        assertNull(state.error)
    }

    @Test
    fun `OnInputChanged recorta al maximo y habilita canSend`() = runTest {
        val env = build()
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        val long = "a".repeat(SupportChatUiState.INPUT_MAX_LENGTH + 10)
        env.vm.onEvent(SupportChatEvent.OnInputChanged(long))
        advanceUntilIdle()

        val state = env.vm.uiState.value
        assertEquals(SupportChatUiState.INPUT_MAX_LENGTH, state.input.length)
        assertTrue(state.canSend)
    }

    @Test
    fun `OnSend persiste turno USER, llama a la AI y persiste turno MODEL`() = runTest {
        val ai = FakeSupportChatAi(defaultResult = AiResult.Success("respuesta"))
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("hola"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        assertEquals(1, ai.calls.size)
        assertEquals("hola", ai.calls.single().userMessage)

        val stored = env.dao.currentItems()
        assertEquals(2, stored.size)
        assertEquals(ChatRole.USER.id, stored.first().roleId)
        assertEquals("hola", stored.first().content)
        assertEquals(ChatRole.MODEL.id, stored.last().roleId)
        assertEquals("respuesta", stored.last().content)

        val state = env.vm.uiState.value
        assertEquals("", state.input)
        assertFalse(state.isGenerating)
        assertFalse(state.lastReplyFromFallback)
    }

    @Test
    fun `respuesta fromFallback activa la bandera en el estado`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Success("respuesta local", fromFallback = true)
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("hola"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        assertTrue(env.vm.uiState.value.lastReplyFromFallback)
    }

    @Test
    fun `DismissFallbackNotice limpia la bandera sin tocar mensajes`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Success("respuesta local", fromFallback = true)
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()
        env.vm.onEvent(SupportChatEvent.OnInputChanged("hola"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.DismissFallbackNotice)
        advanceUntilIdle()

        assertFalse(env.vm.uiState.value.lastReplyFromFallback)
        assertEquals(2, env.vm.uiState.value.messages.size)
    }

    @Test
    fun `error NETWORK preserva mensaje USER, expone retryableInput y habilita canRetry`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Error(
                kind = AiResult.ErrorKind.NETWORK,
                message = "timeout"
            )
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("¿puedes ayudarme?"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        val state = env.vm.uiState.value
        assertNotNull(state.error)
        assertEquals(AiResult.ErrorKind.NETWORK, state.error?.kind)
        assertEquals("¿puedes ayudarme?", state.error?.retryableInput)
        assertTrue(state.canRetry)

        val stored = env.dao.currentItems()
        assertEquals(1, stored.size)
        assertEquals(ChatRole.USER.id, stored.single().roleId)
    }

    @Test
    fun `error TIMEOUT permite reintento`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Error(
                kind = AiResult.ErrorKind.TIMEOUT,
                message = "read timed out"
            )
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("hola"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        val state = env.vm.uiState.value
        assertEquals(AiResult.ErrorKind.TIMEOUT, state.error?.kind)
        assertEquals("hola", state.error?.retryableInput)
        assertTrue(state.canRetry)
    }

    @Test
    fun `error SAFETY_BLOCKED no permite reintento`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Error(
                kind = AiResult.ErrorKind.SAFETY_BLOCKED,
                message = "bloqueado"
            )
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("algo"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        val state = env.vm.uiState.value
        assertNull(state.error?.retryableInput)
        assertFalse(state.canRetry)
    }

    @Test
    fun `OnRetry reutiliza el input fallido sin duplicar el turno USER persistido`() = runTest {
        val ai = FakeSupportChatAi()
        // primer intento falla, segundo (retry) tiene éxito
        ai.nextResult = AiResult.Error(AiResult.ErrorKind.NETWORK, "timeout")
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("probar"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        assertTrue(env.vm.uiState.value.canRetry)

        ai.nextResult = AiResult.Success("ahora sí respondo")
        env.vm.onEvent(SupportChatEvent.OnRetry)
        advanceUntilIdle()

        // Solo se debe haber persistido UN turno USER en total + el MODEL del retry.
        val users = env.dao.currentItems().count { it.roleId == ChatRole.USER.id }
        val models = env.dao.currentItems().count { it.roleId == ChatRole.MODEL.id }
        assertEquals(1, users)
        assertEquals(1, models)
        assertNull(env.vm.uiState.value.error)
    }

    @Test
    fun `OnClearSession genera un nuevo sessionId y limpia la sesion anterior`() = runTest {
        val ai = FakeSupportChatAi(defaultResult = AiResult.Success("ok"))
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        val firstSession = env.vm.uiState.value.sessionId
        env.vm.onEvent(SupportChatEvent.OnInputChanged("hola"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnClearSession)
        advanceUntilIdle()

        val afterSession = env.vm.uiState.value.sessionId
        assertFalse(afterSession == firstSession)
        assertTrue(afterSession.isNotBlank())
        val messagesFromOldSession = env.dao.currentItems().filter { it.sessionId == firstSession }
        assertTrue(messagesFromOldSession.isEmpty())
        assertTrue(env.vm.uiState.value.messages.isEmpty())
    }

    @Test
    fun `DismissError limpia el error pero conserva el input vacio`() = runTest {
        val ai = FakeSupportChatAi(
            defaultResult = AiResult.Error(AiResult.ErrorKind.UNKNOWN, "falló")
        )
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("algo"))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()
        assertNotNull(env.vm.uiState.value.error)

        env.vm.onEvent(SupportChatEvent.DismissError)
        advanceUntilIdle()

        assertNull(env.vm.uiState.value.error)
    }

    @Test
    fun `input vacio o blanco no dispara OnSend ni llama a la AI`() = runTest {
        val ai = FakeSupportChatAi()
        val env = build(ai)
        keepStateSubscribed(env.vm)
        advanceUntilIdle()

        env.vm.onEvent(SupportChatEvent.OnInputChanged("    "))
        env.vm.onEvent(SupportChatEvent.OnSend)
        advanceUntilIdle()

        assertEquals(0, ai.calls.size)
        assertTrue(env.dao.currentItems().isEmpty())
    }

    private data class Env(
        val vm: SupportChatViewModel,
        val dao: FakeChatMessageDao,
        val ai: FakeSupportChatAi,
        val pilotAnalyticsRepository: PilotAnalyticsRepository
    )
}
