package com.example.filtro400

import android.Manifest
import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ListaBlancaScreen(repo: FiltroRepository, modifier: Modifier = Modifier) {
    // Contactos no es una pestaña nueva: se abre desde aquí.
    var mostrarContactos by rememberSaveable { mutableStateOf(false) }

    if (mostrarContactos) {
        ContactosScreen(
            onVolver = { mostrarContactos = false },
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var blancos by remember { mutableStateOf(repo.listarBlancos()) }

    var dialogoAbierto by remember { mutableStateOf(false) }
    var importando by remember { mutableStateOf(false) }
    var dialogoPermiso by remember { mutableStateOf(false) }
    var resultadoImportacion by remember {
        mutableStateOf<ResultadoImportacionBlancos?>(null)
    }

    fun recargar() {
        blancos = repo.listarBlancos()
    }

    fun importarContactos() {
        if (importando) return
        scope.launch {
            importando = true
            val resultado = runCatching {
                withContext(Dispatchers.IO) {
                    val contactos = leerContactos(context.applicationContext)
                    repo.importarBlancos(contactos.map { it.numero })
                }
            }
            importando = false
            resultado.onSuccess {
                recargar()
                resultadoImportacion = it
            }.onFailure {
                Toast.makeText(
                    context,
                    "No se pudieron importar los contactos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            importarContactos()
        } else {
            dialogoPermiso = true
        }
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
                text = "Lista blanca",
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { mostrarContactos = true }) {
                    Text("Contactos")
                }
                Button(onClick = { dialogoAbierto = true }) {
                    Text("Añadir")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (tienePermisoContactos(context)) {
                    importarContactos()
                } else {
                    lanzadorPermiso.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !importando
        ) {
            Text(if (importando) "Importando…" else "Importar contactos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(blancos, key = { _, blanco -> blanco.id }) { indice, blanco ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = blanco.numero,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = {
                            repo.eliminarBlanco(blanco.id)
                            recargar()
                        }) {
                            Text("Eliminar")
                        }
                    }

                    if (indice < blancos.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (dialogoAbierto) {
        DialogoBlanco(
            onCancelar = { dialogoAbierto = false },
            onGuardar = { texto ->
                val guardado = repo.insertarBlanco(texto)
                if (guardado) {
                    dialogoAbierto = false
                    recargar()
                }
                guardado
            }
        )
    }

    if (dialogoPermiso) {
        AlertDialog(
            onDismissRequest = { dialogoPermiso = false },
            title = {
                Text("Permiso necesario")
            },
            text = {
                Text(
                    "Para importar contactos necesitamos permiso para leerlos. " +
                        "Solo se guardarán los números en la lista blanca, sin nombres."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogoPermiso = false
                    lanzadorPermiso.launch(Manifest.permission.READ_CONTACTS)
                }) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogoPermiso = false
                    abrirAjustesDeLaApp(context)
                }) {
                    Text("Abrir ajustes")
                }
            }
        )
    }

    resultadoImportacion?.let { resultado ->
        AlertDialog(
            onDismissRequest = { resultadoImportacion = null },
            title = {
                Text("Importación completada")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Importados: ${resultado.importados}")
                    Text("Ya existentes: ${resultado.duplicados}")
                    Text("Inválidos: ${resultado.invalidos}")
                }
            },
            confirmButton = {
                TextButton(onClick = { resultadoImportacion = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun DialogoBlanco(
    onCancelar: () -> Unit,
    onGuardar: (String) -> Boolean
) {
    val context = LocalContext.current
    val region = remember { NumeroUtil.regionDelDispositivo(context) }

    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

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
            Text("Añadir a lista blanca")
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
