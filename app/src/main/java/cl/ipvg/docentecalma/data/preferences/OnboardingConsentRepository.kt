package cl.ipvg.docentecalma.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface OnboardingConsentRepository {
    val consentAccepted: Flow<Boolean>

    suspend fun isConsentAccepted(): Boolean

    suspend fun setConsentAccepted(accepted: Boolean)
}

@Singleton
class OnboardingConsentRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OnboardingConsentRepository {

    override val consentAccepted: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[DataStorePreferenceKeys.onboardingConsentAccepted] ?: false }

    override suspend fun isConsentAccepted(): Boolean =
        try {
            consentAccepted.first()
        } catch (_: Exception) {
            false
        }

    override suspend fun setConsentAccepted(accepted: Boolean) {
        dataStore.edit { prefs ->
            prefs[DataStorePreferenceKeys.onboardingConsentAccepted] = accepted
        }
    }
}
