package cl.ipvg.docentecalma.safety

import android.util.Log
import cl.ipvg.docentecalma.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de transición sin backend. Cumple dos objetivos:
 *
 * 1. En compilaciones `debug`, dejar **trazado** en `logcat` que el evento existió,
 *    con una severidad explícita y sin contenido sensible. En `release` no se escribe
 *    en log (menos ruido y menor percepción de vigilancia en dispositivos depurados).
 * 2. Mantener una **cola en memoria** acotada (los últimos N eventos) que
 *    podría conectarse a una pantalla interna o a un emisor remoto en el
 *    futuro, sin cambiar el contrato del [RiskEventSink].
 *
 * No persiste en disco ni en red. No expone datos personales: el `excerptHash`
 * es derivado y el `pseudoUserRef` se genera fuera de aquí.
 */
@Singleton
class LocalRiskEventSink @Inject constructor() : RiskEventSink {

    private val ringBuffer: ArrayDeque<RiskEvent> = ArrayDeque()

    override suspend fun emit(event: RiskEvent) {
        if (BuildConfig.DEBUG) {
            Log.w(
                TAG,
                "RiskEvent emitted id=${event.eventId} cat=${event.category} " +
                    "sev=${event.severity} src=${event.source} v=${event.appVersion}"
            )
        }
        synchronized(ringBuffer) {
            ringBuffer.addLast(event)
            while (ringBuffer.size > MAX_BUFFERED_EVENTS) {
                ringBuffer.removeFirst()
            }
        }
    }

    /** Snapshot de eventos retenidos. Pensado para futura UI interna o tests. */
    fun snapshot(): List<RiskEvent> = synchronized(ringBuffer) { ringBuffer.toList() }

    /** Vacía la cola en memoria (p. ej. tras un borrado explícito de datos locales). */
    fun clearBufferedEvents() {
        synchronized(ringBuffer) {
            ringBuffer.clear()
        }
    }

    private companion object {
        const val TAG: String = "DocenteCalmaRisk"
        const val MAX_BUFFERED_EVENTS: Int = 50
    }
}
