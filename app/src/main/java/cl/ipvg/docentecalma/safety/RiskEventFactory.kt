package cl.ipvg.docentecalma.safety

import cl.ipvg.docentecalma.BuildConfig
import cl.ipvg.docentecalma.ai.RiskCategory
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crea [RiskEvent] aplicando reglas de minimización:
 * - El texto original NO se almacena: se calcula un hash truncado para
 *   permitir deduplicación sin reidentificar el contenido.
 * - El identificador de usuario es un seudónimo de instalación (no PII).
 * - El identificador de sesión se recibe de la capa que invoca (chat) y
 *   debe ser opaco al usuario final.
 *
 * Este factory es el único lugar autorizado para construir eventos: así se
 * garantiza que ninguna llamada futura introduzca, por error, PII en el
 * payload.
 */
@Singleton
class RiskEventFactory @Inject constructor(
    private val pseudonymSource: InstallPseudonymSource
) {

    fun create(
        category: RiskCategory,
        userText: String,
        sessionRef: String,
        source: RiskEvent.Source = RiskEvent.Source.SUPPORT_CHAT,
        now: Instant = Instant.now()
    ): RiskEvent = RiskEvent(
        eventId = UUID.randomUUID().toString(),
        category = category,
        severity = category.toSeverity(),
        occurredAt = now,
        pseudoUserRef = pseudonymSource.installPseudonym(),
        sessionRef = sessionRef.take(MAX_SESSION_REF_LENGTH),
        excerptHash = excerptHash(userText),
        source = source,
        appVersion = BuildConfig.VERSION_NAME
    )

    private fun excerptHash(userText: String): String {
        val excerpt = userText.trim().take(MAX_EXCERPT_FOR_HASH)
        if (excerpt.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(excerpt.toByteArray())
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
            .take(HASH_PREFIX_LENGTH)
    }

    private companion object {
        const val MAX_EXCERPT_FOR_HASH: Int = 200
        const val HASH_PREFIX_LENGTH: Int = 16
        const val MAX_SESSION_REF_LENGTH: Int = 64
    }
}
