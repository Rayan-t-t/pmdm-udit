package com.example.filtro400

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegistroScreen(repo: FiltroRepository, modifier: Modifier = Modifier) {
    var registros by remember { mutableStateOf(repo.listarRegistro()) }
    var totalBloqueadas by remember { mutableStateOf(repo.totalBloqueadas()) }
    var totalPermitidas by remember { mutableStateOf(repo.totalPermitidas()) }
    var bloqueosPorRegla by remember { mutableStateOf(repo.bloqueosPorRegla()) }
    var dialogoBorrar by remember { mutableStateOf(false) }

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    fun recargar() {
        registros = repo.listarRegistro()
        totalBloqueadas = repo.totalBloqueadas()
        totalPermitidas = repo.totalPermitidas()
        bloqueosPorRegla = repo.bloqueosPorRegla()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { dialogoBorrar = true }) {
                Text("Borrar registro")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tarjetas de estadísticas.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TarjetaEstadistica(
                titulo = "Bloqueadas",
                valor = totalBloqueadas,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                titulo = "Permitidas",
                valor = totalPermitidas,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Reglas aplicadas",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (bloqueosPorRegla.isEmpty()) {
                    Text(
                        text = "Sin datos",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    bloqueosPorRegla.forEach { (regla, total) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = regla,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = total.toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(registros, key = { _, registro -> registro.id }) { _, registro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (registro.simulada) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = registro.numeroEnmascarado,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = registro.decision,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (registro.decision == "BLOQUEADA") {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Regla: ${registro.regla}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        registro.detalle?.let { detalle ->
                            Text(
                                text = "Detalle: $detalle",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatoFecha.format(Date(registro.fecha)),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (registro.simulada) {
                                Text(
                                    text = "SIMULADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogoBorrar) {
        AlertDialog(
            onDismissRequest = { dialogoBorrar = false },
            title = { Text("Borrar registro") },
            text = {
                Text("¿Seguro que quieres borrar todo el historial? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    repo.borrarRegistro()
                    dialogoBorrar = false
                    recargar()
                }) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoBorrar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaEstadistica(
    titulo: String,
    valor: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
