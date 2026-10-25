package com.example.filtro400

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun FiltrosScreen(repo: FiltroRepository, modifier: Modifier = Modifier) {
    var filtros by remember { mutableStateOf(repo.listar()) }

    var dialogoAbierto by remember { mutableStateOf(false) }
    var filtroEditando by remember { mutableStateOf<Filtro?>(null) }

    fun recargar() {
        filtros = repo.listar()
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
                text = "Prefijos bloqueados",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = {
                filtroEditando = null
                dialogoAbierto = true
            }) {
                Text("Añadir")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(filtros, key = { _, filtro -> filtro.id }) { indice, filtro ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = filtro.activo,
                            onCheckedChange = { activo ->
                                repo.setActivo(filtro.id, activo)
                                recargar()
                            }
                        )
                        val pais = if (filtro.codigoPais.isEmpty()) {
                            "Cualquier país"
                        } else {
                            "+${filtro.codigoPais}"
                        }
                        Text(
                            text = "$pais · ${filtro.prefijo}",
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = {
                            filtroEditando = filtro
                            dialogoAbierto = true
                        }) {
                            Text("Editar")
                        }
                        TextButton(onClick = {
                            repo.eliminar(filtro.id)
                            recargar()
                        }) {
                            Text("Eliminar")
                        }
                    }

                    if (indice < filtros.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (dialogoAbierto) {
        val enEdicion = filtroEditando
        DialogoFiltro(
            filtro = enEdicion,
            onCancelar = { dialogoAbierto = false },
            onGuardar = { prefijo, codigoPais ->
                val guardado = if (enEdicion == null) {
                    repo.insertar(prefijo, codigoPais)
                } else {
                    repo.actualizar(enEdicion.id, prefijo, codigoPais)
                }
                if (guardado) {
                    dialogoAbierto = false
                    recargar()
                }
                guardado
            }
        )
    }
}

// Diálogo reutilizado para añadir (filtro == null) y para editar.
@Composable
private fun DialogoFiltro(
    filtro: Filtro?,
    onCancelar: () -> Unit,
    onGuardar: (prefijo: String, codigoPais: String) -> Boolean
) {
    val context = LocalContext.current
    val paises = remember { NumeroUtil.paises() }
    val region = remember { NumeroUtil.regionDelDispositivo(context) }

    var prefijo by remember(filtro) { mutableStateOf(filtro?.prefijo.orEmpty()) }
    var pais by remember(filtro) {
        mutableStateOf(
            when {
                // Al añadir, partimos del país del dispositivo.
                filtro == null -> paises.firstOrNull { it.iso == region }
                // Código vacío = cualquier país.
                filtro.codigoPais.isEmpty() -> null
                else -> paises.firstOrNull { it.codigo == filtro.codigoPais }
                    // Por si la BD tuviera un código que ya no conocemos.
                    ?: Pais(
                        iso = "",
                        nombre = "País ${filtro.codigoPais}",
                        codigo = filtro.codigoPais,
                        bandera = "🏳️"
                    )
            }
        )
    }
    var selectorAbierto by remember { mutableStateOf(false) }
    var error by remember(filtro) { mutableStateOf<String?>(null) }

    val mensajeError = error
    val textoPais = pais?.let {
        "${it.bandera} ${it.nombre} (+${it.codigo})"
    } ?: "🌍 Cualquier país"

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (filtro == null) "Añadir prefijo" else "Editar prefijo")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = prefijo,
                    onValueChange = { nuevo ->
                        // Solo dígitos, aunque el teclado numérico no lo garantice.
                        prefijo = nuevo.filter { it.isDigit() }
                        error = null
                    },
                    label = { Text("Prefijo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mensajeError != null,
                    supportingText = {
                        if (mensajeError != null) {
                            Text(mensajeError)
                        }
                    },
                    singleLine = true
                )
                OutlinedButton(
                    onClick = { selectorAbierto = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(textoPais)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (prefijo.isEmpty()) {
                    error = "No puede estar vacío"
                } else if (!onGuardar(prefijo, pais?.codigo.orEmpty())) {
                    error = "Ese prefijo ya existe en ese país"
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

    if (selectorAbierto) {
        DialogoPais(
            paises = paises,
            seleccionado = pais,
            onSeleccionar = {
                pais = it
                error = null
                selectorAbierto = false
            },
            onCancelar = { selectorAbierto = false }
        )
    }
}

@Composable
private fun DialogoPais(
    paises: List<Pais>,
    seleccionado: Pais?,
    onSeleccionar: (Pais?) -> Unit,
    onCancelar: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }

    val filtrados = remember(busqueda, paises) {
        val consulta = busqueda.trim()
        if (consulta.isEmpty()) {
            paises
        } else {
            paises.filter { pais ->
                pais.nombre.contains(consulta, ignoreCase = true) ||
                    pais.codigo.contains(consulta) ||
                    pais.iso.contains(consulta, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Elegir país") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar por nombre o código") },
                    singleLine = true
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    // Siempre la primera opción, sin importar la búsqueda.
                    item(key = "cualquier-pais") {
                        FilaPais(
                            texto = "🌍 Cualquier país",
                            seleccionado = seleccionado == null,
                            onClick = { onSeleccionar(null) }
                        )
                    }
                    items(filtrados, key = { it.iso.ifEmpty { it.codigo } }) { pais ->
                        FilaPais(
                            texto = "${pais.bandera} ${pais.nombre} (+${pais.codigo})",
                            seleccionado = seleccionado?.iso == pais.iso &&
                                seleccionado.codigo == pais.codigo,
                            onClick = { onSeleccionar(pais) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun FilaPais(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        if (seleccionado) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
