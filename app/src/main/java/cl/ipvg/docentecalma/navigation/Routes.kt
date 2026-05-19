package cl.ipvg.docentecalma.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Catálogo único de rutas de navegación.
 *
 * - [pattern] es el string que consume Navigation Compose (puede contener
 *   placeholders tipo `{argName}` para rutas con argumentos).
 * - [arguments] declara los argumentos tipados que pertenecen a cada ruta.
 * - Para rutas con parámetros, usa `build(...)` para construir el path real;
 *   nunca concatenes strings en el call-site.
 *
 * Regla del proyecto: **ningún archivo fuera del paquete `navigation`** debe
 * conocer strings de rutas. Los consumidores (pantallas) reciben callbacks
 * `() -> Unit` o `(Arg) -> Unit` vía `NavActions`.
 */
sealed class Routes(val pattern: String) {
    open val arguments: List<NamedNavArgument> = emptyList()

    data object Splash : Routes("splash")

    data object Onboarding : Routes("onboarding")

    data object Home : Routes("home")

    data object EmotionalCheckIn : Routes("checkin")

    data object Recommendations : Routes("recommendations/{$ARG_CHECK_IN_ID}") {
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_CHECK_IN_ID) {
                type = NavType.LongType
                defaultValue = INVALID_CHECK_IN_ID
            }
        )

        fun build(checkInId: Long): String = "recommendations/$checkInId"
    }

    data object ClassroomGuidance : Routes("classroom")

    data object QuickExercises : Routes("exercises")

    data object SupportChat : Routes("chat")

    data object History : Routes("history")

    data object Progress : Routes("progress")

    data object SelfAssessment : Routes("self_assessment")

    data object Privacy : Routes("privacy")

    /** Pantalla interna: agregados de eventos del piloto (solo en el dispositivo). */
    data object PilotMetrics : Routes("internal/pilot_metrics")

    data object Micromodules : Routes("micromodules")

    data object MicromoduleDetail : Routes("micromodule/{$ARG_MODULE_ID}") {
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_MODULE_ID) {
                type = NavType.StringType
            }
        )

        fun build(moduleId: String): String = "micromodule/$moduleId"
    }

    companion object {
        const val ARG_CHECK_IN_ID: String = "checkInId"
        const val INVALID_CHECK_IN_ID: Long = -1L
        const val ARG_MODULE_ID: String = "moduleId"
    }
}
