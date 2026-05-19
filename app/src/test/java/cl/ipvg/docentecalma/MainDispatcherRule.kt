package cl.ipvg.docentecalma

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla JUnit4 que reemplaza el `Main` dispatcher de corrutinas por un
 * `TestDispatcher` durante el test y lo restaura al finalizar.
 *
 * Útil para probar ViewModels que usan `viewModelScope` (que internamente
 * lanza en `Dispatchers.Main.immediate`). Con [UnconfinedTestDispatcher] las
 * corrutinas se ejecutan de forma inmediata, lo que simplifica las
 * aserciones secuenciales.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
