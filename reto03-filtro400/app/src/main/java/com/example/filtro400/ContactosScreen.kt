package com.example.filtro400

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

// Contacto ya normalizado para mostrar y marcar. No se guarda en la BD.
internal data class Contacto(
    val nombre: String,
    val numero: String,
    // null si el número no se pudo interpretar.
    val e164: String?
)

@Composable
fun ContactosScreen(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // El permiso solo se pide al abrir esta pantalla.
    var permisoConcedido by remember {
        mutableStateOf(tienePermisoContactos(context))
    }
    var contactos by remember { mutableStateOf<List<Contacto>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) {
            lanzadorPermiso.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Al volver de Ajustes comprobamos de nuevo el permiso.
    val cicloVida = LocalLifecycleOwner.current
    DisposableEffect(cicloVida) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoConcedido = tienePermisoContactos(context)
            }
        }
        cicloVida.lifecycle.addObserver(observador)
        onDispose {
            cicloVida.lifecycle.removeObserver(observador)
        }
    }

    // La lectura de contactos se hace siempre fuera del hilo principal.
    LaunchedEffect(permisoConcedido) {
        if (!permisoConcedido) {
            contactos = emptyList()
            return@LaunchedEffect
        }
        cargando = true
        contactos = withContext(Dispatchers.IO) {
            leerContactos(context.applicationContext)
        }
        cargando = false
    }

    BackHandler(onBack = onVolver)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contactos",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onVolver) {
                Text("← Volver")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!permisoConcedido) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Para mostrar tus contactos necesitamos permiso para leerlos. " +
                        "Solo se usan en esta pantalla: no se guardan en la base de datos.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    lanzadorPermiso.launch(Manifest.permission.READ_CONTACTS)
                }) {
                    Text("Reintentar")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { abrirAjustesDeLaApp(context) }) {
                    Text("Abrir ajustes de la app")
                }
            }
        } else {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre o número") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val consulta = busqueda.trim()
                val filtrados = remember(consulta, contactos) {
                    if (consulta.isEmpty()) {
                        contactos
                    } else {
                        contactos.filter { contacto ->
                            contacto.nombre.contains(consulta, ignoreCase = true) ||
                                contacto.numero.contains(consulta, ignoreCase = true)
                        }
                    }
                }

                if (filtrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (contactos.isEmpty()) {
                                "No hay contactos para mostrar."
                            } else {
                                "Ningún contacto coincide con la búsqueda."
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filtrados, key = { it.numero }) { contacto ->
                            FilaContacto(
                                contacto = contacto,
                                onLlamar = { abrirMarcador(context, contacto.numero) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaContacto(
    contacto: Contacto,
    onLlamar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contacto.nombre,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = contacto.numero,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onLlamar) {
            Text("Llamar")
        }
    }
}

// Abre el marcador del teléfono con el número escrito; no llama directamente.
private fun abrirMarcador(context: Context, numero: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", numero, null))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No hay ninguna aplicación de teléfono disponible",
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun tienePermisoContactos(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED
}

internal fun abrirAjustesDeLaApp(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Hay dispositivos sin esta pantalla de ajustes.
    }
}

// Lee nombre y número (nunca fotos) y deduplica por número normalizado.
internal fun leerContactos(context: Context): List<Contacto> {
    val region = NumeroUtil.regionDelDispositivo(context)
    val proyeccion = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    val resultado = mutableListOf<Contacto>()
    val vistos = HashSet<String>()

    try {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            proyeccion,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val columnaNombre = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val columnaNumero = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            while (cursor.moveToNext()) {
                val nombre = cursor.getString(columnaNombre)?.trim().orEmpty()
                val numeroCrudo = cursor.getString(columnaNumero)?.trim().orEmpty()
                if (numeroCrudo.isEmpty()) continue

                val normalizado = runCatching {
                    NumeroUtil.normalizar(numeroCrudo, region)
                }.getOrNull()

                // Si no se puede interpretar, se conserva el número tal cual.
                val numero = normalizado?.e164 ?: numeroCrudo
                val clave = normalizado?.e164
                    ?: numeroCrudo.filter { it.isDigit() || it == '+' }

                if (clave.isEmpty() || !vistos.add(clave)) continue

                resultado.add(
                    Contacto(
                        nombre = nombre.ifBlank { "Sin nombre" },
                        numero = numero,
                        e164 = normalizado?.e164
                    )
                )
            }
        }
    } catch (e: Exception) {
        // Sin permiso o con un proveedor defectuoso, devolvemos lista vacía.
        return emptyList()
    }

    val colador = Collator.getInstance().apply { strength = Collator.PRIMARY }
    return resultado.sortedWith(
        Comparator { a, b -> colador.compare(a.nombre, b.nombre) }
    )
}
