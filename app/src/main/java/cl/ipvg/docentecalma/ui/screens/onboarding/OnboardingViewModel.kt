package cl.ipvg.docentecalma.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.analytics.PilotEventNames
import cl.ipvg.docentecalma.data.preferences.OnboardingConsentRepository
import cl.ipvg.docentecalma.data.repository.PilotAnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingConsentRepository: OnboardingConsentRepository,
    private val pilotAnalyticsRepository: PilotAnalyticsRepository
) : ViewModel() {

    fun acceptConsent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            onboardingConsentRepository.setConsentAccepted(true)
            pilotAnalyticsRepository.record(PilotEventNames.CONSENT_ACCEPTED)
            pilotAnalyticsRepository.record(PilotEventNames.ONBOARDING_COMPLETED)
            onSuccess()
        }
    }
}
