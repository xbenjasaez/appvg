package cl.ipvg.docentecalma.safety

/**
 * Punto único de salida para [RiskEvent]. Permite intercambiar la estrategia
 * de envío sin tocar el clasificador ni el ViewModel del chat:
 * - Hoy: implementación local (log + cache en memoria).
 * - Mañana: envío a backend con cola persistente y reintentos.
 *
 * Las implementaciones DEBEN ser seguras de invocar desde cualquier hilo
 * (el chat puede emitir desde corrutinas en `Default`/`IO`).
 */
interface RiskEventSink {
    suspend fun emit(event: RiskEvent)
}
