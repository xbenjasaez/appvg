package cl.ipvg.docentecalma.ui.screens.emotionalcheckin

import cl.ipvg.docentecalma.MainDispatcherRule
import cl.ipvg.docentecalma.data.repository.EmotionalRepository
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.testing.FakeEmotionalCheckInDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmotionalCheckInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildVm(
        dao: FakeEmotionalCheckInDao = FakeEmotionalCheckInDao()
    ): Pair<EmotionalCheckInViewModel, FakeEmotionalCheckInDao> {
        val repo = EmotionalRepository(dao)
        return EmotionalCheckInViewModel(repo) to dao
    }

    @Test
    fun `estado inicial tiene intensidad default y ninguna emocion seleccionada`() {
        val (vm, _) = buildVm()
        val state = vm.uiState.value

        assertNull(state.selectedEmotion)
        assertEquals(EmotionalCheckInUiState.DEFAULT_INTENSITY, state.intensity)
        assertEquals("", state.note)
        assertFalse(state.canSave)
        assertFalse(state.isSaving)
    }

    @Test
    fun `OnEmotionSelected actualiza la emocion y habilita guardar`() {
        val (vm, _) = buildVm()
        vm.onEvent(EmotionalCheckInEvent.OnEmotionSelected(Emotion.STRESS))

        val state = vm.uiState.value
        assertEquals(Emotion.STRESS, state.selectedEmotion)
        assertTrue(state.canSave)
    }

    @Test
    fun `OnIntensityChanged recorta a los limites validos`() {
        val (vm, _) = buildVm()

        vm.onEvent(EmotionalCheckInEvent.OnIntensityChanged(99))
        assertEquals(5, vm.uiState.value.intensity)

        vm.onEvent(EmotionalCheckInEvent.OnIntensityChanged(-3))
        assertEquals(1, vm.uiState.value.intensity)

        vm.onEvent(EmotionalCheckInEvent.OnIntensityChanged(4))
        assertEquals(4, vm.uiState.value.intensity)
    }

    @Test
    fun `OnNoteChanged recorta al maximo permitido`() {
        val (vm, _) = buildVm()
        val longNote = "a".repeat(EmotionalCheckInUiState.NOTE_MAX_LENGTH + 50)

        vm.onEvent(EmotionalCheckInEvent.OnNoteChanged(longNote))

        assertEquals(
            EmotionalCheckInUiState.NOTE_MAX_LENGTH,
            vm.uiState.value.note.length
        )
    }

    @Test
    fun `OnSave sin emocion seleccionada produce error y no persiste`() = runTest {
        val (vm, dao) = buildVm()

        vm.onEvent(EmotionalCheckInEvent.OnSave)

        val state = vm.uiState.value
        assertNotNull(state.error)
        assertTrue(dao.currentItems().isEmpty())
    }

    @Test
    fun `OnSave exitoso resetea estado y emite efecto Saved con id`() = runTest {
        val (vm, dao) = buildVm()
        vm.onEvent(EmotionalCheckInEvent.OnEmotionSelected(Emotion.ANXIETY))
        vm.onEvent(EmotionalCheckInEvent.OnIntensityChanged(4))
        vm.onEvent(EmotionalCheckInEvent.OnNoteChanged("pre-clase"))

        vm.onEvent(EmotionalCheckInEvent.OnSave)

        val effect = vm.effects.first()
        assertTrue(effect is EmotionalCheckInEffect.Saved)
        val savedId = (effect as EmotionalCheckInEffect.Saved).checkInId
        assertTrue(savedId > 0L)
        assertEquals(1, dao.currentItems().size)

        val stateAfter = vm.uiState.value
        assertNull(stateAfter.selectedEmotion)
        assertEquals("", stateAfter.note)
        assertFalse(stateAfter.isSaving)
        assertNull(stateAfter.error)
    }

    @Test
    fun `OnSave persiste exactamente los valores elegidos`() = runTest {
        val (vm, dao) = buildVm()
        vm.onEvent(EmotionalCheckInEvent.OnEmotionSelected(Emotion.SADNESS))
        vm.onEvent(EmotionalCheckInEvent.OnIntensityChanged(2))
        vm.onEvent(EmotionalCheckInEvent.OnNoteChanged("   "))

        vm.onEvent(EmotionalCheckInEvent.OnSave)

        val stored = dao.currentItems().single()
        assertEquals(Emotion.SADNESS.id, stored.emotionId)
        assertEquals(2, stored.intensity)
        assertNull("nota en blanco debe guardarse como null", stored.note)
    }

    @Test
    fun `DismissError limpia el error sin tocar otros campos`() {
        val (vm, _) = buildVm()
        vm.onEvent(EmotionalCheckInEvent.OnSave) // genera error por no haber emoción

        assertNotNull(vm.uiState.value.error)

        vm.onEvent(EmotionalCheckInEvent.DismissError)

        assertNull(vm.uiState.value.error)
    }
}
