package cl.ipvg.docentecalma.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.ipvg.docentecalma.ui.screens.classroomguidance.ClassroomGuidanceScreen
import cl.ipvg.docentecalma.ui.screens.emotionalcheckin.EmotionalCheckInScreen
import cl.ipvg.docentecalma.ui.screens.history.HistoryScreen
import cl.ipvg.docentecalma.ui.screens.home.HomeNavActions
import cl.ipvg.docentecalma.ui.screens.home.HomeScreen
import cl.ipvg.docentecalma.ui.screens.micromodules.MicromoduleReaderScreen
import cl.ipvg.docentecalma.ui.screens.micromodules.MicromodulesListScreen
import cl.ipvg.docentecalma.ui.screens.onboarding.OnboardingScreen
import cl.ipvg.docentecalma.ui.screens.pilotmetrics.PilotMetricsScreen
import cl.ipvg.docentecalma.ui.screens.privacy.PrivacyScreen
import cl.ipvg.docentecalma.ui.screens.progress.ProgressScreen
import cl.ipvg.docentecalma.ui.screens.quickexercises.QuickExercisesScreen
import cl.ipvg.docentecalma.ui.screens.recommendations.RecommendationsScreen
import cl.ipvg.docentecalma.ui.screens.selfassessment.SelfAssessmentNavActions
import cl.ipvg.docentecalma.ui.screens.selfassessment.SelfAssessmentScreen
import cl.ipvg.docentecalma.ui.screens.splash.SplashScreen
import cl.ipvg.docentecalma.ui.screens.supportchat.SupportChatScreen

/**
 * Grafo de navegación único de la app.
 *
 * Reglas:
 * - Los patrones de ruta viven en [Routes]. Este archivo nunca los concatena.
 * - Las acciones de navegación viven en [NavActions]. Este archivo crea la
 *   instancia y expone funciones a cada pantalla como callbacks.
 * - Las pantallas reciben callbacks `() -> Unit` / `(Arg) -> Unit`; no ven
 *   ni `NavHostController` ni `Routes`.
 */
@Composable
fun DocenteCalmaNavHost(navController: NavHostController) {
    val nav = rememberNavActions(navController)

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.pattern
    ) {
        composable(Routes.Splash.pattern) {
            SplashScreen(
                onNavigateToHome = nav::fromSplashToHome,
                onNavigateToOnboarding = nav::fromSplashToOnboarding
            )
        }

        composable(Routes.Onboarding.pattern) {
            OnboardingScreen(onConsentAccepted = nav::fromOnboardingToHome)
        }

        composable(Routes.Home.pattern) {
            HomeScreen(
                navActions = HomeNavActions(
                    onNavigateToCheckIn = nav::toCheckIn,
                    onNavigateToClassroomGuidance = nav::toClassroomGuidance,
                    onNavigateToQuickExercises = nav::toQuickExercises,
                    onNavigateToSupportChat = nav::toSupportChat,
                    onNavigateToHistory = nav::toHistory,
                    onNavigateToProgress = nav::toProgress,
                    onNavigateToSelfAssessment = nav::toSelfAssessment,
                    onNavigateToPrivacy = nav::toPrivacy,
                    onNavigateToMicromodules = nav::toMicromodules
                )
            )
        }

        composable(Routes.EmotionalCheckIn.pattern) {
            EmotionalCheckInScreen(
                onBack = nav::back,
                onNavigateToRecommendations = nav::toRecommendations
            )
        }

        composable(
            route = Routes.Recommendations.pattern,
            arguments = Routes.Recommendations.arguments
        ) {
            RecommendationsScreen(
                onBack = nav::back,
                onOpenExercises = nav::toQuickExercises,
                onOpenChat = nav::toSupportChat,
                onOpenMicromodule = nav::toMicromodule
            )
        }

        composable(Routes.ClassroomGuidance.pattern) {
            ClassroomGuidanceScreen(onBack = nav::back)
        }

        composable(Routes.QuickExercises.pattern) {
            QuickExercisesScreen(onBack = nav::back)
        }

        composable(Routes.Micromodules.pattern) {
            MicromodulesListScreen(
                onBack = nav::back,
                onOpenModule = nav::toMicromodule
            )
        }

        composable(
            route = Routes.MicromoduleDetail.pattern,
            arguments = Routes.MicromoduleDetail.arguments
        ) {
            MicromoduleReaderScreen(
                onBack = nav::back,
                onOpenExercises = nav::toQuickExercises
            )
        }

        composable(Routes.SupportChat.pattern) {
            SupportChatScreen(onBack = nav::back)
        }

        composable(Routes.History.pattern) {
            HistoryScreen(
                onBack = nav::back,
                onStartCheckIn = nav::toCheckIn
            )
        }

        composable(Routes.Progress.pattern) {
            ProgressScreen(
                onBack = nav::back,
                onStartCheckIn = nav::toCheckIn
            )
        }

        composable(Routes.SelfAssessment.pattern) {
            SelfAssessmentScreen(
                onBack = nav::back,
                navActions = SelfAssessmentNavActions(
                    onOpenCheckIn = nav::toCheckIn,
                    onOpenExercises = nav::toQuickExercises,
                    onOpenClassroom = nav::toClassroomGuidance,
                    onOpenChat = nav::toSupportChat
                )
            )
        }

        composable(Routes.Privacy.pattern) {
            PrivacyScreen(
                onBack = nav::back,
                onReviewConsent = nav::fromPrivacyToOnboarding,
                onOpenPilotMetrics = nav::toPilotMetrics
            )
        }

        composable(Routes.PilotMetrics.pattern) {
            PilotMetricsScreen(onBack = nav::back)
        }
    }
}
