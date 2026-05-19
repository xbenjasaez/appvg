package cl.ipvg.docentecalma.safety

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Genera y persiste un seudónimo opaco por instalación.
 *
 * - No se asocia a cuenta, correo, RUT ni dispositivo físico identificable.
 * - Vive en `SharedPreferences` privado de la app: si el usuario desinstala
 *   la app, el seudónimo desaparece.
 * - Es la única forma autorizada de identificar agrupaciones de eventos sin
 *   exponer datos personales.
 */
@Singleton
class InstallPseudonymProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallPseudonymSource {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    override fun installPseudonym(): String {
        val existing = prefs.getString(KEY_PSEUDONYM, null)
        if (!existing.isNullOrBlank()) return existing
        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_PSEUDONYM, generated).apply()
        return generated
    }

    private companion object {
        const val PREF_FILE: String = "docentecalma_safety_prefs"
        const val KEY_PSEUDONYM: String = "install_pseudonym"
    }
}
