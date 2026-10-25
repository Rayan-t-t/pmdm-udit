package com.example.filtro400

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

// CallScreeningService: Android nos avisa de cada llamada entrante
// ANTES de que suene, y nosotros decidimos qué hacer con ella.
// Solo funciona con números que NO están en la agenda del usuario.
class FiltroLlamadasService : CallScreeningService() {

    // Leemos la base de datos en cada llamada: si el usuario cambia los
    // prefijos, el filtro se aplica sin reiniciar nada.
    private val repo by lazy { FiltroRepository(applicationContext) }

    // Android llama a este método automáticamente por cada llamada.
    override fun onScreenCall(callDetails: Call.Details) {
        var bloquear = false
        var numeroEnmascarado = "***"

        try {
            // Solo filtramos llamadas entrantes (no las que haces tú).
            val esEntrante = callDetails.callDirection == Call.Details.DIRECTION_INCOMING

            // handle = la dirección de la llamada, algo como "tel:+34400123456".
            // schemeSpecificPart nos devuelve solo el número. Si es un número
            // oculto, viene vacío y no lo bloqueamos.
            val numero = callDetails.handle?.schemeSpecificPart.orEmpty()
            numeroEnmascarado = MotorFiltrado.enmascarar(numero)

            val decision = if (!esEntrante || numero.isBlank()) {
                // No es entrante o el número está oculto: no se bloquea nunca.
                Decision(false, "ninguna", null)
            } else {
                // Normalizamos con la región del dispositivo. Cualquier fallo
                // (null o excepción) deja pasar la llamada como "ninguna".
                val normalizado = try {
                    NumeroUtil.normalizar(
                        numero,
                        NumeroUtil.regionDelDispositivo(applicationContext)
                    )
                } catch (e: Exception) {
                    // No volcamos el número en el log: solo el tipo de error.
                    Log.w(
                        "Filtro400",
                        "No se pudo normalizar el número entrante (${e.javaClass.simpleName})"
                    )
                    null
                }

                if (normalizado == null) {
                    Decision(false, "ninguna", null)
                } else {
                    // A partir de aquí trabajamos siempre con E.164.
                    numeroEnmascarado = MotorFiltrado.enmascarar(normalizado.e164)
                    MotorFiltrado.evaluar(normalizado, repo.reglasActivas())
                }
            }

            bloquear = decision.bloquear

            // El registro va aparte: si falla, la decisión de bloquear no cambia.
            try {
                // En las reglas "numero" y "lista_blanca" el detalle es el
                // número completo, así que no lo guardamos. Solo el prefijo
                // es seguro de almacenar.
                val detalleSeguro = if (decision.regla == "prefijo") decision.detalle else null
                repo.registrar(
                    decision.copy(detalle = detalleSeguro),
                    numeroEnmascarado,
                    simulada = false
                )
            } catch (e: Exception) {
                Log.w("Filtro400", "No se pudo registrar la llamada", e)
            }

            // Log sin el número completo.
            Log.d(
                "Filtro400",
                "Llamada $numeroEnmascarado -> " +
                    "${if (bloquear) "BLOQUEADA" else "PERMITIDA"} (${decision.regla})"
            )
        } catch (e: Exception) {
            // Ante cualquier fallo inesperado, dejamos pasar la llamada.
            bloquear = false
            Log.e("Filtro400", "Error al evaluar la llamada", e)
        } finally {
            // Siempre hay que responder, también cuando dejamos pasar la llamada.
            try {
                val respuesta = CallResponse.Builder()
                    .setDisallowCall(bloquear)      // impide que suene
                    .setRejectCall(bloquear)        // la rechaza
                    .setSkipNotification(bloquear)  // sin notificación de llamada perdida
                    .build()
                respondToCall(callDetails, respuesta)
            } catch (e: Exception) {
                Log.e("Filtro400", "No se pudo responder a la llamada", e)
            }
        }
    }
}
