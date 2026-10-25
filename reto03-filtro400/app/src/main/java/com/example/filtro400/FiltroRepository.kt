package com.example.filtro400

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Un prefijo bloqueado tal y como lo guardamos en la base de datos.
data class Filtro(
    val id: Long,
    val prefijo: String,
    val codigoPais: String,
    val activo: Boolean,
    val prioridad: Int
)

// Números concretos que queremos bloquear, además de los prefijos.
data class NumeroBloqueado(
    val id: Long,
    val numero: String,
    val activo: Boolean
)

// Números que nunca deben bloquearse.
data class NumeroBlanco(
    val id: Long,
    val numero: String
)

// Resultado de una importación masiva a la lista blanca.
data class ResultadoImportacionBlancos(
    val importados: Int,
    val duplicados: Int,
    val invalidos: Int
)

// Historial de decisiones del filtro (reales o simuladas).
data class RegistroLlamada(
    val id: Long,
    val fecha: Long,
    val numeroEnmascarado: String,
    val decision: String,
    val regla: String,
    val detalle: String?,
    val simulada: Boolean
)

private const val NOMBRE_BD = "filtro400.db"
private const val VERSION_BD = 3
private const val TABLA = "filtros"
private const val TABLA_NUMEROS = "numeros_bloqueados"
private const val TABLA_BLANCA = "lista_blanca"
private const val TABLA_REGISTRO = "registro_llamadas"

// Valores de la columna "decision" del registro.
private const val DECISION_BLOQUEADA = "BLOQUEADA"
private const val DECISION_PERMITIDA = "PERMITIDA"

// SQL compartido entre onCreate y onUpgrade para que el esquema no se desincronice.
private const val SQL_CREAR_FILTROS = """
    CREATE TABLE filtros (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        prefijo TEXT NOT NULL,
        activo INTEGER NOT NULL DEFAULT 1,
        prioridad INTEGER NOT NULL DEFAULT 0,
        codigo_pais TEXT NOT NULL DEFAULT '',
        UNIQUE (codigo_pais, prefijo)
    )
"""

private const val SQL_AGREGAR_PRIORIDAD = """
    ALTER TABLE filtros ADD COLUMN prioridad INTEGER NOT NULL DEFAULT 0
"""

// La tabla vieja tenía UNIQUE(prefijo) y SQLite no permite quitarlo con ALTER,
// así que se reconstruye conservando los datos.
private const val SQL_CREAR_FILTROS_NUEVA = """
    CREATE TABLE filtros_nueva (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        prefijo TEXT NOT NULL,
        activo INTEGER NOT NULL DEFAULT 1,
        prioridad INTEGER NOT NULL DEFAULT 0,
        codigo_pais TEXT NOT NULL DEFAULT '',
        UNIQUE (codigo_pais, prefijo)
    )
"""

private const val SQL_MIGRAR_FILTROS = """
    INSERT INTO filtros_nueva (id, prefijo, activo, prioridad, codigo_pais)
    SELECT id, prefijo, activo, prioridad, '34' FROM filtros
"""

private const val SQL_RENOMBRAR_FILTROS = """
    ALTER TABLE filtros_nueva RENAME TO filtros
"""

private const val SQL_NORMALIZAR_NUMEROS = """
    UPDATE numeros_bloqueados SET numero = '+34' || numero WHERE numero NOT LIKE '+%'
"""

private const val SQL_NORMALIZAR_BLANCOS = """
    UPDATE lista_blanca SET numero = '+34' || numero WHERE numero NOT LIKE '+%'
"""

private const val SQL_CREAR_NUMEROS_BLOQUEADOS = """
    CREATE TABLE numeros_bloqueados (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        numero TEXT NOT NULL UNIQUE,
        activo INTEGER NOT NULL DEFAULT 1
    )
"""

private const val SQL_CREAR_LISTA_BLANCA = """
    CREATE TABLE lista_blanca (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        numero TEXT NOT NULL UNIQUE
    )
"""

private const val SQL_CREAR_REGISTRO_LLAMADAS = """
    CREATE TABLE registro_llamadas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        fecha INTEGER NOT NULL,
        numero_enmascarado TEXT NOT NULL,
        decision TEXT NOT NULL,
        regla TEXT NOT NULL,
        detalle TEXT,
        simulada INTEGER NOT NULL DEFAULT 0
    )
"""

private const val SQL_CREAR_INDICE_REGISTRO_FECHA = """
    CREATE INDEX idx_registro_fecha ON registro_llamadas(fecha)
"""

// SQLiteOpenHelper se encarga de crear/abrir la base de datos.
private class FiltroDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    override fun onCreate(db: SQLiteDatabase) {
        // Esquema final completo (versión 3).
        db.execSQL(SQL_CREAR_FILTROS)
        db.execSQL(SQL_CREAR_NUMEROS_BLOQUEADOS)
        db.execSQL(SQL_CREAR_LISTA_BLANCA)
        db.execSQL(SQL_CREAR_REGISTRO_LLAMADAS)
        db.execSQL(SQL_CREAR_INDICE_REGISTRO_FECHA)

        // Mantenemos el comportamiento de la Fase 1: el 400 de España
        // viene bloqueado.
        val valores = ContentValues().apply {
            put("prefijo", "400")
            put("activo", 1)
            put("codigo_pais", "34")
        }
        db.insert(TABLA, null, valores)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migración 1 -> 2: columnas y tablas que faltaban.
        if (oldVersion < 2) {
            db.execSQL(SQL_AGREGAR_PRIORIDAD)
            db.execSQL(SQL_CREAR_NUMEROS_BLOQUEADOS)
            db.execSQL(SQL_CREAR_LISTA_BLANCA)
            db.execSQL(SQL_CREAR_REGISTRO_LLAMADAS)
            db.execSQL(SQL_CREAR_INDICE_REGISTRO_FECHA)
        }

        // Migración 2 -> 3: prefijos por país y números en E.164.
        if (oldVersion < 3) {
            db.execSQL(SQL_CREAR_FILTROS_NUEVA)
            db.execSQL(SQL_MIGRAR_FILTROS)
            db.execSQL("DROP TABLE $TABLA")
            db.execSQL(SQL_RENOMBRAR_FILTROS)
            db.execSQL(SQL_NORMALIZAR_NUMEROS)
            db.execSQL(SQL_NORMALIZAR_BLANCOS)
        }
    }
}

// Repositorio: toda la lectura/escritura pasa por aquí.
class FiltroRepository(context: Context) {

    // applicationContext evita guardar una referencia a una Activity.
    private val appContext = context.applicationContext
    private val helper = FiltroDbHelper(appContext)

    // Región actual del dispositivo, para interpretar números nacionales.
    private fun normalizarE164(numero: String): String? {
        return try {
            val region = NumeroUtil.regionDelDispositivo(appContext)
            NumeroUtil.normalizar(numero, region)?.e164
        } catch (e: Exception) {
            // Si no se puede interpretar, se rechaza la entrada.
            null
        }
    }

    // ---------------------------------------------------------------------
    // Prefijos
    // ---------------------------------------------------------------------

    // Devuelve todos los filtros ordenados por id ascendente.
    fun listar(): List<Filtro> {
        val columnas = arrayOf("id", "prefijo", "codigo_pais", "activo", "prioridad")
        return helper.readableDatabase
            .query(TABLA, columnas, null, null, null, null, "id ASC")
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.aFiltro())
                    }
                }
            }
    }

    // Solo los prefijos de las filas activas.
    fun prefijosActivos(): List<String> {
        return helper.readableDatabase
            .query(
                TABLA,
                arrayOf("prefijo"),
                "activo = ?",
                arrayOf("1"),
                null,
                null,
                "id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(cursor.getColumnIndexOrThrow("prefijo")))
                    }
                }
            }
    }

    // false si el par (codigoPais, prefijo) ya existe.
    fun insertar(prefijo: String, codigoPais: String): Boolean {
        val valores = ContentValues().apply {
            put("prefijo", prefijo)
            put("activo", 1)
            put("codigo_pais", codigoPais.trim().removePrefix("+"))
        }
        return try {
            helper.writableDatabase.insertOrThrow(TABLA, null, valores)
            true
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    // false si el par (codigoPais, prefijo) está duplicado en otra fila.
    fun actualizar(id: Long, prefijo: String, codigoPais: String): Boolean {
        val valores = ContentValues().apply {
            put("prefijo", prefijo)
            put("codigo_pais", codigoPais.trim().removePrefix("+"))
        }
        return try {
            val filas = helper.writableDatabase.update(
                TABLA,
                valores,
                "id = ?",
                arrayOf(id.toString())
            )
            filas > 0
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    // Activa o desactiva un filtro concreto.
    fun setActivo(id: Long, activo: Boolean) {
        val valores = ContentValues().apply {
            put("activo", if (activo) 1 else 0)
        }
        helper.writableDatabase.update(
            TABLA,
            valores,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // Borra un filtro por su id.
    fun eliminar(id: Long) {
        helper.writableDatabase.delete(
            TABLA,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // Cambia la prioridad de un prefijo (mayor = se evalúa antes).
    fun setPrioridad(id: Long, prioridad: Int) {
        val valores = ContentValues().apply {
            put("prioridad", prioridad)
        }
        helper.writableDatabase.update(
            TABLA,
            valores,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // ---------------------------------------------------------------------
    // Números bloqueados
    // ---------------------------------------------------------------------

    fun listarNumeros(): List<NumeroBloqueado> {
        return helper.readableDatabase
            .query(
                TABLA_NUMEROS,
                arrayOf("id", "numero", "activo"),
                null,
                null,
                null,
                null,
                "id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.aNumeroBloqueado())
                    }
                }
            }
    }

    // Guarda el número en E.164; false si ya existe o no se puede normalizar.
    fun insertarNumero(numero: String): Boolean {
        val e164 = normalizarE164(numero) ?: return false
        val valores = ContentValues().apply {
            put("numero", e164)
            put("activo", 1)
        }
        return try {
            helper.writableDatabase.insertOrThrow(TABLA_NUMEROS, null, valores)
            true
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    // Guarda el número en E.164; false si ya existe o no se puede normalizar.
    fun actualizarNumero(id: Long, numero: String): Boolean {
        val e164 = normalizarE164(numero) ?: return false
        val valores = ContentValues().apply {
            put("numero", e164)
        }
        return try {
            val filas = helper.writableDatabase.update(
                TABLA_NUMEROS,
                valores,
                "id = ?",
                arrayOf(id.toString())
            )
            filas > 0
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    fun setNumeroActivo(id: Long, activo: Boolean) {
        val valores = ContentValues().apply {
            put("activo", if (activo) 1 else 0)
        }
        helper.writableDatabase.update(
            TABLA_NUMEROS,
            valores,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun eliminarNumero(id: Long) {
        helper.writableDatabase.delete(
            TABLA_NUMEROS,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // ---------------------------------------------------------------------
    // Lista blanca
    // ---------------------------------------------------------------------

    fun listarBlancos(): List<NumeroBlanco> {
        return helper.readableDatabase
            .query(
                TABLA_BLANCA,
                arrayOf("id", "numero"),
                null,
                null,
                null,
                null,
                "id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.aNumeroBlanco())
                    }
                }
            }
    }

    // Guarda el número en E.164; false si ya existe o no se puede normalizar.
    fun insertarBlanco(numero: String): Boolean {
        val e164 = normalizarE164(numero) ?: return false
        val valores = ContentValues().apply {
            put("numero", e164)
        }
        return try {
            helper.writableDatabase.insertOrThrow(TABLA_BLANCA, null, valores)
            true
        } catch (e: SQLiteConstraintException) {
            false
        }
    }

    fun eliminarBlanco(id: Long) {
        helper.writableDatabase.delete(
            TABLA_BLANCA,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // Importa varios números en una sola transacción. Los normaliza a E.164,
    // descarta los que no se pueden interpretar y cuenta los ya existentes.
    fun importarBlancos(numeros: List<String>): ResultadoImportacionBlancos {
        var invalidos = 0
        val validos = ArrayList<String>(numeros.size)
        for (numero in numeros) {
            val e164 = normalizarE164(numero)
            if (e164 == null) {
                invalidos++
            } else {
                validos.add(e164)
            }
        }

        if (validos.isEmpty()) {
            return ResultadoImportacionBlancos(
                importados = 0,
                duplicados = 0,
                invalidos = invalidos
            )
        }

        var importados = 0
        var duplicados = 0
        val db = helper.writableDatabase

        db.beginTransaction()
        try {
            for (numero in validos) {
                val valores = ContentValues().apply {
                    put("numero", numero)
                }
                val id = db.insertWithOnConflict(
                    TABLA_BLANCA,
                    null,
                    valores,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
                if (id == -1L) {
                    duplicados++
                } else {
                    importados++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return ResultadoImportacionBlancos(
            importados = importados,
            duplicados = duplicados,
            invalidos = invalidos
        )
    }

    // ---------------------------------------------------------------------
    // Reglas activas para el motor
    // ---------------------------------------------------------------------

    fun reglasActivas(): ReglasActivas {
        val listaBlanca = helper.readableDatabase
            .query(
                TABLA_BLANCA,
                arrayOf("numero"),
                null,
                null,
                null,
                null,
                "id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(cursor.getColumnIndexOrThrow("numero")))
                    }
                }
            }

        val numerosBloqueados = helper.readableDatabase
            .query(
                TABLA_NUMEROS,
                arrayOf("numero"),
                "activo = ?",
                arrayOf("1"),
                null,
                null,
                "id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.getString(cursor.getColumnIndexOrThrow("numero")))
                    }
                }
            }

        val prefijos = helper.readableDatabase
            .query(
                TABLA,
                arrayOf("codigo_pais", "prefijo", "prioridad"),
                "activo = ?",
                arrayOf("1"),
                null,
                null,
                "prioridad DESC, id ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            PrefijoRegla(
                                codigoPais = cursor.getString(
                                    cursor.getColumnIndexOrThrow("codigo_pais")
                                ),
                                prefijo = cursor.getString(
                                    cursor.getColumnIndexOrThrow("prefijo")
                                ),
                                prioridad = cursor.getInt(
                                    cursor.getColumnIndexOrThrow("prioridad")
                                )
                            )
                        )
                    }
                }
            }

        return ReglasActivas(
            listaBlanca = listaBlanca,
            numerosBloqueados = numerosBloqueados,
            prefijos = prefijos
        )
    }

    // ---------------------------------------------------------------------
    // Registro de llamadas
    // ---------------------------------------------------------------------

    fun registrar(decision: Decision, numeroEnmascarado: String, simulada: Boolean) {
        val valores = ContentValues().apply {
            put("fecha", System.currentTimeMillis())
            put("numero_enmascarado", numeroEnmascarado)
            put("decision", if (decision.bloquear) DECISION_BLOQUEADA else DECISION_PERMITIDA)
            put("regla", decision.regla)
            put("detalle", decision.detalle)
            put("simulada", if (simulada) 1 else 0)
        }
        helper.writableDatabase.insert(TABLA_REGISTRO, null, valores)
    }

    fun listarRegistro(limite: Int = 200): List<RegistroLlamada> {
        return helper.readableDatabase
            .query(
                TABLA_REGISTRO,
                arrayOf(
                    "id",
                    "fecha",
                    "numero_enmascarado",
                    "decision",
                    "regla",
                    "detalle",
                    "simulada"
                ),
                null,
                null,
                null,
                null,
                "fecha DESC, id DESC",
                limite.toString()
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(cursor.aRegistroLlamada())
                    }
                }
            }
    }

    fun borrarRegistro() {
        helper.writableDatabase.delete(TABLA_REGISTRO, null, null)
    }

    // ---------------------------------------------------------------------
    // Estadísticas
    // ---------------------------------------------------------------------

    fun totalBloqueadas(): Int = contarRegistro(DECISION_BLOQUEADA)

    fun totalPermitidas(): Int = contarRegistro(DECISION_PERMITIDA)

    // Agrupa por regla y detalle; mayor número de bloqueos primero.
    fun bloqueosPorRegla(): List<Pair<String, Int>> {
        return helper.readableDatabase
            .query(
                TABLA_REGISTRO,
                arrayOf("regla", "detalle", "COUNT(*) AS total"),
                null,
                null,
                "regla, detalle",
                null,
                "total DESC, regla ASC"
            )
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val regla = cursor.getString(cursor.getColumnIndexOrThrow("regla"))
                        val detalle = cursor.getString(cursor.getColumnIndexOrThrow("detalle"))
                        val total = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
                        val clave = if (detalle.isNullOrBlank()) regla else "$regla: $detalle"
                        add(clave to total)
                    }
                }
            }
    }

    private fun contarRegistro(valorDecision: String): Int {
        return helper.readableDatabase
            .query(
                TABLA_REGISTRO,
                arrayOf("COUNT(*)"),
                "decision = ?",
                arrayOf(valorDecision),
                null,
                null,
                null
            )
            .use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
    }

    // ---------------------------------------------------------------------
    // Mapeo de Cursor a modelos
    // ---------------------------------------------------------------------

    private fun Cursor.aFiltro(): Filtro = Filtro(
        id = getLong(getColumnIndexOrThrow("id")),
        prefijo = getString(getColumnIndexOrThrow("prefijo")),
        codigoPais = getString(getColumnIndexOrThrow("codigo_pais")),
        activo = getInt(getColumnIndexOrThrow("activo")) == 1,
        prioridad = getInt(getColumnIndexOrThrow("prioridad"))
    )

    private fun Cursor.aNumeroBloqueado(): NumeroBloqueado = NumeroBloqueado(
        id = getLong(getColumnIndexOrThrow("id")),
        numero = getString(getColumnIndexOrThrow("numero")),
        activo = getInt(getColumnIndexOrThrow("activo")) == 1
    )

    private fun Cursor.aNumeroBlanco(): NumeroBlanco = NumeroBlanco(
        id = getLong(getColumnIndexOrThrow("id")),
        numero = getString(getColumnIndexOrThrow("numero"))
    )

    private fun Cursor.aRegistroLlamada(): RegistroLlamada = RegistroLlamada(
        id = getLong(getColumnIndexOrThrow("id")),
        fecha = getLong(getColumnIndexOrThrow("fecha")),
        numeroEnmascarado = getString(getColumnIndexOrThrow("numero_enmascarado")),
        decision = getString(getColumnIndexOrThrow("decision")),
        regla = getString(getColumnIndexOrThrow("regla")),
        detalle = getString(getColumnIndexOrThrow("detalle")),
        simulada = getInt(getColumnIndexOrThrow("simulada")) == 1
    )
}
