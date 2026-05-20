package cl.ipvg.docentecalma.ui.screens.tranquillight

import androidx.compose.ui.geometry.Offset

enum class ParticleKind {
    Burst,
    Trail,
    Ambient
}

data class LightParticle(
    val id: Long,
    val origin: Offset,
    val createdAtMs: Long,
    val lifespanMs: Long = 600L,
    val kind: ParticleKind = ParticleKind.Burst,
    val velocity: Offset = Offset.Zero
) {
    fun alphaAt(nowMs: Long): Float {
        val elapsed = (nowMs - createdAtMs).coerceAtLeast(0L)
        if (elapsed >= lifespanMs) return 0f
        val t = elapsed.toFloat() / lifespanMs
        val fade = when (kind) {
            ParticleKind.Burst -> 1f - t * t
            ParticleKind.Trail -> 1f - t
            ParticleKind.Ambient -> (1f - t) * 0.85f
        }
        return fade.coerceIn(0f, 1f)
    }

    fun positionAt(nowMs: Long): Offset {
        val elapsed = (nowMs - createdAtMs).coerceAtLeast(0L).toFloat()
        val driftFactor = when (kind) {
            ParticleKind.Burst -> elapsed / lifespanMs
            else -> 0f
        }
        return Offset(
            origin.x + velocity.x * driftFactor,
            origin.y + velocity.y * driftFactor
        )
    }
}
