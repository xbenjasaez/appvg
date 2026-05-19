package cl.ipvg.docentecalma.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.ipvg.docentecalma.data.preferences.OnboardingConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingConsentRepository: OnboardingConsentRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val acceptedAsync = async { onboardingConsentRepository.isConsentAccepted() }
            delay(SPLASH_DURATION_MS)
            val accepted = acceptedAsync.await()
            _destination.value =
                if (accepted) SplashDestination.Home else SplashDestination.Onboarding
        }
    }

    fun consumeDestination() {
        _destination.value = null
    }

    sealed class SplashDestination {
        data object Home : SplashDestination()
        data object Onboarding : SplashDestination()
    }
}

private const val SPLASH_DURATION_MS: Long = 1400L
