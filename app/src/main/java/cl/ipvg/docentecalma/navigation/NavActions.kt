package cl.ipvg.docentecalma.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController

/**
 * Encapsula todas las acciones de navegación que la app puede realizar.
 *
 * - Vive solo dentro del paquete `navigation`. Las pantallas nunca ven el
 *   `NavHostController`: reciben referencias a funciones de esta clase.
 * - Centraliza las `NavOptions` (back stack, singleTop, popUp) para evitar
 *   divergencias entre llamadas repetidas.
 */
@Stable
class NavActions(private val navController: NavHostController) {

    fun back() {
        navController.popBackStack()
    }

    /**
     * Reemplaza el splash por home. Limpia el back stack para que el botón
     * atrás desde Home no vuelva a la pantalla de carga.
     */
    fun fromSplashToHome() {
        navController.navigate(Routes.Home.pattern) {
            popUpTo(Routes.Splash.pattern) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun fromSplashToOnboarding() {
        navController.navigate(Routes.Onboarding.pattern) {
            popUpTo(Routes.Splash.pattern) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun fromOnboardingToHome() {
        navController.navigate(Routes.Home.pattern) {
            popUpTo(Routes.Onboarding.pattern) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun toCheckIn() {
        navController.navigate(Routes.EmotionalCheckIn.pattern) {
            launchSingleTop = true
        }
    }

    fun toClassroomGuidance() {
        navController.navigate(Routes.ClassroomGuidance.pattern) {
            launchSingleTop = true
        }
    }

    fun toQuickExercises() {
        navController.navigate(Routes.QuickExercises.pattern) {
            launchSingleTop = true
        }
    }

    fun toBreathingWithVirgi() {
        navController.navigate(Routes.BreathingWithVirgi.pattern) {
            launchSingleTop = true
        }
    }

    fun toTraceAndRelease() {
        navController.navigate(Routes.TraceAndRelease.pattern) {
            launchSingleTop = true
        }
    }

    fun toTranquilLight() {
        navController.navigate(Routes.TranquilLight.pattern) {
            launchSingleTop = true
        }
    }

    fun toCloudAndClarity() {
        navController.navigate(Routes.CloudAndClarity.pattern) {
            launchSingleTop = true
        }
    }

    fun toSupportChat() {
        navController.navigate(Routes.SupportChat.pattern) {
            launchSingleTop = true
        }
    }

    fun toHistory() {
        navController.navigate(Routes.History.pattern) {
            launchSingleTop = true
        }
    }

    fun toProgress() {
        navController.navigate(Routes.Progress.pattern) {
            launchSingleTop = true
        }
    }

    fun toSelfAssessment() {
        navController.navigate(Routes.SelfAssessment.pattern) {
            launchSingleTop = true
        }
    }

    fun toPrivacy() {
        navController.navigate(Routes.Privacy.pattern) {
            launchSingleTop = true
        }
    }

    fun toPilotMetrics() {
        navController.navigate(Routes.PilotMetrics.pattern) {
            launchSingleTop = true
        }
    }

    fun toMicromodules() {
        navController.navigate(Routes.Micromodules.pattern) {
            launchSingleTop = true
        }
    }

    fun toMicromodule(moduleId: String) {
        navController.navigate(Routes.MicromoduleDetail.build(moduleId)) {
            launchSingleTop = true
        }
    }

    /**
     * Vuelve a mostrar el onboarding de consentimiento. Limpia el back stack hasta
     * antes de Home para que el flujo sea coherente tras revisar el aviso.
     */
    fun fromPrivacyToOnboarding() {
        navController.navigate(Routes.Onboarding.pattern) {
            popUpTo(Routes.Home.pattern) { inclusive = true }
            launchSingleTop = true
        }
    }

    /**
     * Navega a Recommendations y retira `EmotionalCheckIn` del back stack:
     * al apretar atrás desde Recommendations se vuelve a Home, no al formulario.
     */
    fun toRecommendations(checkInId: Long) {
        navController.navigate(Routes.Recommendations.build(checkInId)) {
            popUpTo(Routes.Home.pattern) { inclusive = false }
            launchSingleTop = true
        }
    }
}

@Composable
fun rememberNavActions(navController: NavHostController): NavActions =
    remember(navController) { NavActions(navController) }
