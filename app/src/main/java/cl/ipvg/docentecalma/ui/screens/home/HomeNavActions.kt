package cl.ipvg.docentecalma.ui.screens.home

/**
 * Agrupa las acciones de navegación que `HomeScreen` puede disparar.
 *
 * Se declara en `ui/screens/home` (no en `navigation/`) porque el contrato
 * pertenece a la pantalla: si mañana se agrega un módulo nuevo se actualiza
 * aquí sin tocar el paquete de navegación.
 */
data class HomeNavActions(
    val onNavigateToCheckIn: () -> Unit,
    val onNavigateToClassroomGuidance: () -> Unit,
    val onNavigateToQuickExercises: () -> Unit,
    val onNavigateToBreathingWithVirgi: () -> Unit,
    val onNavigateToTraceAndRelease: () -> Unit,
    val onNavigateToTranquilLight: () -> Unit,
    val onNavigateToCloudAndClarity: () -> Unit,
    val onNavigateToSupportChat: () -> Unit,
    val onNavigateToHistory: () -> Unit,
    val onNavigateToProgress: () -> Unit,
    val onNavigateToSelfAssessment: () -> Unit,
    val onNavigateToPrivacy: () -> Unit,
    val onNavigateToMicromodules: () -> Unit
)
