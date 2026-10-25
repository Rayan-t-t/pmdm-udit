package com.example.filtro400

// Resultado de aplicar las reglas a un número.
data class Decision(
    val bloquear: Boolean,
    val regla: String,
    val detalle: String?
)

// Un prefijo activo junto al país al que se limita ("" = cualquier país).
data class PrefijoRegla(
    val codigoPais: String,
    val prefijo: String,
    val prioridad: Int
)

// Copia de las reglas activas que usa el motor para decidir.
data class ReglasActivas(
    val listaBlanca: List<String>,
    val numerosBloqueados: List<String>,
    val prefijos: List<PrefijoRegla>
)

// Motor puro: no depende de Android ni de la base de datos.
object MotorFiltrado {

    fun evaluar(numero: NumeroNormalizado, reglas: ReglasActivas): Decision {
        if (numero.e164.isEmpty()) {
            return Decision(false, "ninguna", null)
        }

        // 1. La lista blanca tiene la máxima prioridad (igualdad de E.164).
        if (reglas.listaBlanca.any { it == numero.e164 }) {
            return Decision(false, "lista_blanca", numero.e164)
        }

        // 2. Números bloqueados explícitamente (igualdad de E.164).
        if (reglas.numerosBloqueados.any { it == numero.e164 }) {
            return Decision(true, "numero", numero.e164)
        }

        // 3. Prefijos de mayor a menor prioridad; gana el primero que coincida.
        val prefijo = reglas.prefijos
            .sortedByDescending { it.prioridad }
            .firstOrNull { regla ->
                if (regla.prefijo.isEmpty()) {
                    false
                } else if (regla.codigoPais.isNotEmpty()) {
                    numero.e164.startsWith("+${regla.codigoPais}${regla.prefijo}")
                } else {
                    numero.nacional.startsWith(regla.prefijo)
                }
            }
        if (prefijo != null) {
            return Decision(true, "prefijo", prefijo.prefijo)
        }

        // 4. Ninguna regla coincide.
        return Decision(false, "ninguna", null)
    }

    // Deja visibles los 3 primeros y los 3 últimos dígitos.
    fun enmascarar(numero: String): String {
        val digitos = numero.filter { it.isDigit() }
        if (digitos.length <= 6) {
            return "***"
        }
        return digitos.take(3) + "*".repeat(digitos.length - 6) + digitos.takeLast(3)
    }
}
