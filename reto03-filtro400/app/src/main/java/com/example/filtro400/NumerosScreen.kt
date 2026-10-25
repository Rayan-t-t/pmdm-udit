package com.example.filtro400

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun NumerosScreen(repo: FiltroRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var numeros by remember { mutableStateOf(repo.listarNumeros()) }

    var dialogoAbierto by remember { mutableStateOf(false) }
    var numeroEditando by remember { mutableStateOf<NumeroBloqueado?>(null) }
    var mensajeCsv by remember { mutableStateOf<MensajeCsv?>(null) }

    fun recargar() {
        numeros = repo.listarNumeros()
    }

    // Storage Access Framework: el usuario elige el archivo en el selector
    // del sistema. No hacen falta permisos de almacenamiento.
    val lanzadorExportar = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contenido = CsvListas.exportar(repo)
        val fallo = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { salida ->
                salida.write(contenido.toByteArray(Charsets.UTF_8))
            } ?: error("No se pudo abrir el archivo")
        }.exceptionOrNull()
        mensajeCsv = if (fallo == null) {
            MensajeCsv("Exportar CSV", "CSV exportado correctamente.")
        } else {
            MensajeCsv("Exportar CSV", "No se pudo exportar el CSV.")
        }
    }

    val lanzadorImportar = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val lectura = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("No se pudo abrir el archivo")
        }
        mensajeCsv = lectura.fold(
            onSuccess = { texto ->
                val resultado = CsvListas.importar(texto, repo)
                recargar()
                MensajeCsv(
                    titulo = "Importar CSV",
                    texto = "Importados: ${resultado.importados}\n" +
                        "Duplicados: ${resultado.duplicados}\n" +
                        "Inválidos: ${resultado.invalidos}"
                )
            },
            onFailure = {
                MensajeCsv("Importar CSV", "No se pudo leer el archivo.")
            }
        )
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
                text = "Números bloqueados",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = {
                numeroEditando = null
                dialogoAbierto = true
            }) {
                Text("Añadir")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Importación/exportación del CSV de listas mediante SAF.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { lanzadorImportar.launch(arrayOf("*/*")) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Importar CSV")
            }
            OutlinedButton(
                onClick = { lanzadorExportar.launch("filtro400.csv") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Exportar CSV")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(numeros, key = { _, numero -> numero.id }) { indice, numero ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = numero.activo,
                            onCheckedChange = { activo ->
                                repo.setNumeroActivo(numero.id, activo)
                                recargar()
                            }
                        )
                        Text(
                            text = numero.numero,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = {
                            numeroEditando = numero
                            dialogoAbierto = true
                        }) {
                            Text("Editar")
                        }
                        TextButton(onClick = {
                            repo.eliminarNumero(numero.id)
                            recargar()
                        }) {
                            Text("Eliminar")
                        }
                    }

                    if (indice < numeros.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (dialogoAbierto) {
        val enEdicion = numeroEditando
        DialogoNumero(
            numero = enEdicion,
            onCancelar = { dialogoAbierto = false },
            onGuardar = { texto ->
                val guardado = if (enEdicion == null) {
                    repo.insertarNumero(texto)
                } else {
                    repo.actualizarNumero(enEdicion.id, texto)
                }
                if (guardado) {
                    dialogoAbierto = false
                    recargar()
                }
                guardado
            }
        )
    }

    mensajeCsv?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { mensajeCsv = null },
            title = { Text(mensaje.titulo) },
            text = { Text(mensaje.texto) },
            confirmButton = {
                TextButton(onClick = { mensajeCsv = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

// Resumen mostrado tras importar o exportar un CSV.
private data class MensajeCsv(val titulo: String, val texto: String)

// Diálogo reutilizado para añadir (numero == null) y para editar.
@Composable
private fun DialogoNumero(
    numero: NumeroBloqueado?,
    onCancelar: () -> Unit,
    onGuardar: (String) -> Boolean
) {
    val context = LocalContext.current
    val region = remember { NumeroUtil.regionDelDispositivo(context) }

    var texto by remember(numero) { mutableStateOf(numero?.numero.orEmpty()) }
    var error by remember(numero) { mutableStateOf<String?>(null) }

    val normalizado = remember(texto, region) {
        if (texto.isBlank()) {
            null
        } else {
            runCatching { NumeroUtil.normalizar(texto, region) }.getOrNull()
        }
    }
    val invalido = texto.isNotBlank() && normalizado == null
    val mensajeError = error
    val ayuda = when {
        mensajeError != null -> mensajeError
        invalido -> "No se pudo interpretar el número"
        normalizado != null -> "Se guardará como ${normalizado.e164}"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (numero == null) "Añadir número" else "Editar número")
        },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { nuevo ->
                    // Se acepta cualquier formato; se normaliza al guardar.
                    texto = nuevo
                    error = null
                },
                label = { Text("Número") },
                placeholder = { Text("Ej. +34 612 34 56 78") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = mensajeError != null || invalido,
                supportingText = {
                    ayuda?.let { Text(it) }
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val limpio = texto.trim()
                if (limpio.isEmpty()) {
                    error = "No puede estar vacío"
                } else {
                    val numeroNormalizado = runCatching {
                        NumeroUtil.normalizar(limpio, region)
                    }.getOrNull()
                    if (numeroNormalizado == null) {
                        error = "No se pudo interpretar el número"
                    } else if (!onGuardar(numeroNormalizado.e164)) {
                        error = "Ese número ya existe"
                    }
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
