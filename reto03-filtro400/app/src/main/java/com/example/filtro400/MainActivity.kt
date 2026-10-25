package com.example.filtro400

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.filtro400.ui.theme.Filtro400Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Filtro400Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaFiltro()
                }
            }
        }
    }
}

// Preferencias para no repetir la petición de rol en cada apertura.
private const val PREFS_ROL = "filtro400_prefs"
private const val CLAVE_ROL_SOLICITADO = "rol_solicitado"
private const val CLAVE_ROL_ESTUVO_ACTIVO = "rol_estuvo_activo"

// Pestañas de la barra inferior.
private enum class Pestana(val etiqueta: String, val icono: ImageVector) {
    FILTROS("Filtros", Icons.Default.Warning),
    NUMEROS("Números", Icons.Default.Phone),
    BLANCA("Permitidos", Icons.Default.Star),
    REGISTRO("Registro", Icons.AutoMirrored.Filled.List),
    PROBADOR("Probador", Icons.Default.PlayArrow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFiltro() {
    val context = LocalContext.current

    // Un único repositorio para todas las pantallas.
    val repo = remember { FiltroRepository(context.applicationContext) }

    // RoleManager: el servicio de Android que gestiona "roles" como
    // "app de filtrado de llamadas". Sin el rol, nuestro servicio no se activa.
    val roleManager = remember { context.getSystemService(RoleManager::class.java) }

    // SharedPreferences: recuerdan si ya pedimos el rol y si estuvo activo.
    val prefs = remember {
        context.getSharedPreferences(PREFS_ROL, Context.MODE_PRIVATE)
    }

    // Estado que la pantalla "vigila": si el filtro está activo.
    var filtroActivo by remember {
        mutableStateOf(roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING))
    }

    // Protección contra bucles: mientras haya una petición en curso,
    // no se lanza ninguna otra.
    var solicitandoRol by rememberSaveable { mutableStateOf(false) }

    // Pestaña seleccionada: estado simple, sin dependencias de navegación.
    var pestana by remember { mutableStateOf(Pestana.FILTROS) }

    // Cuando el usuario responde al diálogo del sistema, volvemos aquí
    // y volvemos a comprobar si ahora tenemos el rol.
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        solicitandoRol = false
        filtroActivo = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        if (filtroActivo) {
            prefs.edit().putBoolean(CLAVE_ROL_ESTUVO_ACTIVO, true).apply()
        }
    }

    fun lanzarSolicitudRol() {
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        solicitandoRol = true
        lanzador.launch(intent)
    }

    fun comprobarRol() {
        filtroActivo = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

        // Con el rol concedido solo recordamos que estuvo activo.
        if (filtroActivo) {
            prefs.edit().putBoolean(CLAVE_ROL_ESTUVO_ACTIVO, true).apply()
            return
        }

        // Ya hay una petición en curso: no lanzamos otra.
        if (solicitandoRol) return

        val solicitado = prefs.getBoolean(CLAVE_ROL_SOLICITADO, false)
        val estuvoActivo = prefs.getBoolean(CLAVE_ROL_ESTUVO_ACTIVO, false)

        when {
            // Primera vez: se pide automáticamente.
            !solicitado -> {
                prefs.edit().putBoolean(CLAVE_ROL_SOLICITADO, true).apply()
                lanzarSolicitudRol()
            }
            // El usuario cambió de filtro: se vuelve a pedir una vez.
            estuvoActivo -> {
                prefs.edit().putBoolean(CLAVE_ROL_ESTUVO_ACTIVO, false).apply()
                lanzarSolicitudRol()
            }
        }
    }

    // Al abrir la app.
    LaunchedEffect(Unit) {
        comprobarRol()
    }

    // En cada ON_RESUME (volver del diálogo del rol o de Ajustes).
    val cicloVida = LocalLifecycleOwner.current
    DisposableEffect(cicloVida) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                comprobarRol()
            }
        }
        cicloVida.lifecycle.addObserver(observador)
        onDispose {
            cicloVida.lifecycle.removeObserver(observador)
        }
    }

    fun abrirAjustes() {
        try {
            context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            // Algún dispositivo no ofrece esta pantalla de ajustes.
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Filtro 400") }
            )
        },
        bottomBar = {
            BarraNavegacion(
                seleccionada = pestana,
                onSeleccionar = { pestana = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CabeceraRol(
                filtroActivo = filtroActivo,
                onAbrirAjustes = { abrirAjustes() }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (pestana) {
                    Pestana.FILTROS -> FiltrosScreen(repo, Modifier.fillMaxSize())
                    Pestana.NUMEROS -> NumerosScreen(repo, Modifier.fillMaxSize())
                    Pestana.BLANCA -> ListaBlancaScreen(repo, Modifier.fillMaxSize())
                    Pestana.REGISTRO -> RegistroScreen(repo, Modifier.fillMaxSize())
                    Pestana.PROBADOR -> ProbadorScreen(repo, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun CabeceraRol(
    filtroActivo: Boolean,
    onAbrirAjustes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (filtroActivo) "✅ Filtro ACTIVO" else "⚠️ Filtro inactivo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Aviso discreto mientras falte el rol. Al concederlo desaparece.
        if (!filtroActivo) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sin ser el filtro de llamadas predeterminado, " +
                            "la app no puede bloquear llamadas.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = onAbrirAjustes) {
                        Text("Abrir ajustes")
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraNavegacion(
    seleccionada: Pestana,
    onSeleccionar: (Pestana) -> Unit
) {
    NavigationBar {
        Pestana.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == seleccionada,
                onClick = { onSeleccionar(pestana) },
                icon = {
                    Icon(
                        imageVector = pestana.icono,
                        contentDescription = pestana.etiqueta
                    )
                },
                label = {
                    Text(
                        text = pestana.etiqueta,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

