package cl.ipvg.docentecalma.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Última respuesta persistida del prompt breve (1–5 cada ítem), si existe.
 */
data class PostUseFeedbackSnapshot(
    val submittedAtMs: Long,
    val satisfaction: Int,
    val usefulness: Int,
    val ease: Int
)

/**
 * Feedback breve post-uso: persistencia local y reglas de frecuencia (snooze).
 * Usa el mismo [DataStore] que el onboarding para no multiplicar archivos.
 */
interface PostUseFeedbackRepository {

    /**
     * Cuenta una visita a inicio y responde si corresponde mostrar el prompt.
     */
    suspend fun evaluateAfterHomeEntered(): Boolean

    suspend fun recordDismissal()

    suspend fun saveSubmission(
        satisfaction: Int,
        usefulness: Int,
        ease: Int,
        comment: String?
    )

    /**
     * `true` si hay respuestas de feedback post-uso o texto libre guardado localmente.
     * Sirve para habilitar “Borrar historial” cuando solo existe feedback en DataStore.
     */
    val hasStoredFeedback: Flow<Boolean>

    /**
     * Observa la última respuesta completa del cuestionario breve, si el usuario envió el formulario.
     */
    fun observeLastSubmission(): Flow<PostUseFeedbackSnapshot?>

    /**
     * Elimina todo lo persistido del prompt de feedback (puntuaciones, comentario, contadores).
     * No modifica el consentimiento de onboarding en el mismo archivo DataStore.
     */
    suspend fun clearStoredFeedback()
}

@Singleton
class PostUseFeedbackRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PostUseFeedbackRepository {

    override val hasStoredFeedback: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val submitted = prefs[DataStorePreferenceKeys.PostUseFeedback.submittedAtMs] != null
            val comment =
                prefs[DataStorePreferenceKeys.PostUseFeedback.comment]?.isNotBlank() == true
            submitted || comment
        }

    override fun observeLastSubmission(): Flow<PostUseFeedbackSnapshot?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val at = prefs[DataStorePreferenceKeys.PostUseFeedback.submittedAtMs] ?: return@map null
            val sat = prefs[DataStorePreferenceKeys.PostUseFeedback.satisfaction] ?: return@map null
            val use = prefs[DataStorePreferenceKeys.PostUseFeedback.usefulness] ?: return@map null
            val ease = prefs[DataStorePreferenceKeys.PostUseFeedback.ease] ?: return@map null
            PostUseFeedbackSnapshot(
                submittedAtMs = at,
                satisfaction = sat,
                usefulness = use,
                ease = ease
            )
        }

    override suspend fun clearStoredFeedback() {
        dataStore.edit { prefs ->
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.homeSurfaces)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.snoozeUntilMs)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.submittedAtMs)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.satisfaction)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.usefulness)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.ease)
            prefs.remove(DataStorePreferenceKeys.PostUseFeedback.comment)
        }
    }

    override suspend fun evaluateAfterHomeEntered(): Boolean {
        val prefsAfter = dataStore.edit { prefs ->
            val next = (prefs[DataStorePreferenceKeys.PostUseFeedback.homeSurfaces] ?: 0) + 1
            prefs[DataStorePreferenceKeys.PostUseFeedback.homeSurfaces] = next
        }
        val count = prefsAfter[DataStorePreferenceKeys.PostUseFeedback.homeSurfaces] ?: 0
        val snoozeUntil = prefsAfter[DataStorePreferenceKeys.PostUseFeedback.snoozeUntilMs] ?: 0L
        val now = System.currentTimeMillis()
        return count >= MIN_HOME_SURFACES && now >= snoozeUntil
    }

    override suspend fun recordDismissal() {
        dataStore.edit { prefs ->
            prefs[DataStorePreferenceKeys.PostUseFeedback.snoozeUntilMs] =
                System.currentTimeMillis() + DISMISS_SNOOZE_MS
        }
    }

    override suspend fun saveSubmission(
        satisfaction: Int,
        usefulness: Int,
        ease: Int,
        comment: String?
    ) {
        val now = System.currentTimeMillis()
        val trimmed = comment?.trim().orEmpty().take(MAX_COMMENT_LEN)
        dataStore.edit { prefs ->
            prefs[DataStorePreferenceKeys.PostUseFeedback.submittedAtMs] = now
            prefs[DataStorePreferenceKeys.PostUseFeedback.satisfaction] = satisfaction
            prefs[DataStorePreferenceKeys.PostUseFeedback.usefulness] = usefulness
            prefs[DataStorePreferenceKeys.PostUseFeedback.ease] = ease
            prefs[DataStorePreferenceKeys.PostUseFeedback.comment] = trimmed
            prefs[DataStorePreferenceKeys.PostUseFeedback.snoozeUntilMs] = now + SUBMIT_SNOOZE_MS
        }
    }

    companion object {
        private const val MIN_HOME_SURFACES: Int = 4
        private val DISMISS_SNOOZE_MS: Long = 21L * 24 * 60 * 60 * 1000
        private val SUBMIT_SNOOZE_MS: Long = 120L * 24 * 60 * 60 * 1000
        private const val MAX_COMMENT_LEN: Int = 280
    }
}
