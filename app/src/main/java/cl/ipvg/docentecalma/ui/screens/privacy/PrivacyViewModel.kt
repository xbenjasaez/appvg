package cl.ipvg.docentecalma.ui.screens.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.preferences.OnboardingConsentRepository
import cl.ipvg.docentecalma.data.repository.LocalDataCounts
import cl.ipvg.docentecalma.data.repository.LocalPrivacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyUiState(
    val counts: LocalDataCounts = LocalDataCounts(0, 0, 0),
    val consentAccepted: Boolean = false
)

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val localPrivacyRepository: LocalPrivacyRepository,
    private val onboardingConsentRepository: OnboardingConsentRepository
) : ViewModel() {

    val uiState: StateFlow<PrivacyUiState> = combine(
        localPrivacyRepository.observeLocalDataCounts(),
        onboardingConsentRepository.consentAccepted
    ) { counts, consent ->
        PrivacyUiState(counts = counts, consentAccepted = consent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrivacyUiState()
    )

    private val _isClearingHistory = MutableStateFlow(false)
    val isClearingHistory: StateFlow<Boolean> = _isClearingHistory.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun consumeUserMessage() {
        _userMessage.value = null
    }

    fun clearLocalHistoryAfterConfirmation() {
        if (_isClearingHistory.value) return
        viewModelScope.launch {
            _isClearingHistory.value = true
            try {
                localPrivacyRepository.clearAllUserHistory()
                _userMessage.value =
                    "Listo: se borró tu historial en este dispositivo, incluido el feedback breve guardado aquí. " +
                        "No afecta mensajes ya enviados a la IA."
            } catch (_: Exception) {
                _userMessage.value = "No se pudo borrar el historial. Inténtalo de nuevo."
            } finally {
                _isClearingHistory.value = false
            }
        }
    }

    fun resetConsentForReview(onComplete: () -> Unit) {
        viewModelScope.launch {
            onboardingConsentRepository.setConsentAccepted(false)
            onComplete()
        }
    }
}
