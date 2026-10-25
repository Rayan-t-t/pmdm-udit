package com.example.filtro400

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProbadorScreen(repo: FiltroRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var numeroPrueba by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<Decision?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun simular() {
        // Sin dígitos no hay nada que evaluar: no registramos la simulación.
        if (numeroPrueba.none { it.isDigit() }) {
            error = "Introduce un número"
            resultado = null
            return
        }

        // El motor trabaja siempre con un número ya normalizado.
        val normalizado = NumeroUtil.normalizar(
            numeroPrueba,
            NumeroUtil.regionDelDispositivo(context)
        )
        if (normalizado == null) {
            error = "No se pudo interpretar el número"
            resultado = null
            return
        }

        val decision = MotorFiltrado.evaluar(normalizado, repo.reglasActivas())
        // En el registro no guardamos números completos: solo el prefijo.
        val detalleSeguro = if (decision.regla == "prefijo") decision.detalle else null
        // Queda en el historial marcado como simulado: sirve de evidencia de pruebas.
        repo.registrar(
            decision = decision.copy(detalle = detalleSeguro),
            numeroEnmascarado = MotorFiltrado.enmascarar(normalizado.e164),
            simulada = true
        )

        error = null
        resultado = decision
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Probador de números",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Comprueba qué haría el filtro con un número, sin recibir una llamada.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = numeroPrueba,
            onValueChange = { nuevo ->
                // Solo dígitos y el signo "+", igual que en el resto de pantallas.
                numeroPrueba = nuevo.filter { it.isDigit() || it == '+' }
                error = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Número") },
            placeholder = { Text("Ej. +34 400 123 456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = error != null,
            supportingText = {
                error?.let { Text(it) }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { simular() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simular llamada")
        }

        resultado?.let { decision ->
            Spacer(modifier = Modifier.height(16.dp))
            TarjetaResultado(decision)
        }
    }
}

@Composable
private fun TarjetaResultado(decision: Decision) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (decision.bloquear) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (decision.bloquear) "🚫 BLOQUEADA" else "✅ PERMITIDA",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Regla aplicada: ${nombreRegla(decision.regla)}",
                style = MaterialTheme.typography.bodyLarge
            )
            decision.detalle?.let { detalle ->
                Text(
                    text = "Detalle: $detalle",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Guardada en el registro como simulación.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Los códigos internos del motor se muestran con un nombre legible.
private fun nombreRegla(regla: String): String = when (regla) {
    "lista_blanca" -> "lista blanca"
    "numero" -> "número bloqueado"
    "prefijo" -> "prefijo"
    else -> "ninguna"
}
