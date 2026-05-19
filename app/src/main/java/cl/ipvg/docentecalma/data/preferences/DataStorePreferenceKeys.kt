package cl.ipvg.docentecalma.data.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Claves del [androidx.datastore.core.DataStore] compartido (onboarding + feedback).
 * Centralizar aquí facilita auditorías de privacidad y evita colisiones accidentales.
 */
object DataStorePreferenceKeys {

    val onboardingConsentAccepted: Preferences.Key<Boolean> =
        booleanPreferencesKey("onboarding_consent_accepted")

    object PostUseFeedback {
        val homeSurfaces: Preferences.Key<Int> =
            intPreferencesKey("post_use_feedback_home_surfaces")
        val snoozeUntilMs: Preferences.Key<Long> =
            longPreferencesKey("post_use_feedback_snooze_until_ms")
        val submittedAtMs: Preferences.Key<Long> =
            longPreferencesKey("post_use_feedback_submitted_at_ms")
        val satisfaction: Preferences.Key<Int> =
            intPreferencesKey("post_use_feedback_satisfaction")
        val usefulness: Preferences.Key<Int> =
            intPreferencesKey("post_use_feedback_usefulness")
        val ease: Preferences.Key<Int> =
            intPreferencesKey("post_use_feedback_ease")
        val comment: Preferences.Key<String> =
            stringPreferencesKey("post_use_feedback_comment")
    }
}
