package com.example.filtro400

// Tipos de fila admitidos en el CSV de listas.
enum class TipoCsv(val etiqueta: String) {
    BLOQUEADO("bloqueado"),
    BLANCO("blanco"),
    PREFIJO("prefijo");

    companion object {
        fun desde(texto: String): TipoCsv? =
            entries.firstOrNull { it.etiqueta == texto }
    }
}

// Una fila ya validada y normalizada.
data class FilaCsv(val tipo: TipoCsv, val valor: String)

data class ResultadoImportacion(
    val importados: Int,
    val duplicados: Int,
    val invalidos: Int
)

// Importación/exportación del CSV de listas: "tipo,valor".
object CsvListas {

    const val CABECERA = "tipo,valor"

    // ---------------------------------------------------------------------
    // Exportación
    // ---------------------------------------------------------------------

    fun exportar(repo: FiltroRepository): String = generarCsv(
        prefijos = repo.listar().map { it.prefijo },
        bloqueados = repo.listarNumeros().map { it.numero },
        blancos = repo.listarBlancos().map { it.numero }
    )

    // Parte pura: fácil de probar sin base de datos.
    fun generarCsv(
        prefijos: List<String>,
        bloqueados: List<String>,
        blancos: List<String>
    ): String = buildString {
        appendLine(CABECERA)
        bloqueados.forEach { appendLine("${TipoCsv.BLOQUEADO.etiqueta},${limpiarCampo(it)}") }
        blancos.forEach { appendLine("${TipoCsv.BLANCO.etiqueta},${limpiarCampo(it)}") }
        prefijos.forEach { appendLine("${TipoCsv.PREFIJO.etiqueta},${limpiarCampo(it)}") }
    }

    // ---------------------------------------------------------------------
    // Importación
    // ---------------------------------------------------------------------

    fun importar(texto: String, repo: FiltroRepository): ResultadoImportacion {
        val (filas, invalidos) = parsear(texto)

        var importados = 0
        var duplicados = 0

        filas.forEach { fila ->
            // Los métodos insertar* ya devuelven false si el valor existe.
            val insertado = when (fila.tipo) {
                TipoCsv.BLOQUEADO -> repo.insertarNumero(fila.valor)
                TipoCsv.BLANCO -> repo.insertarBlanco(fila.valor)
                // El CSV no guarda el país: mantenemos el significado
                // histórico de la app (prefijos de España).
                TipoCsv.PREFIJO -> repo.insertar(fila.valor, "34")
            }
            if (insertado) importados++ else duplicados++
        }

        return ResultadoImportacion(
            importados = importados,
            duplicados = duplicados,
            invalidos = invalidos
        )
    }

    // Devuelve las filas válidas y cuántas líneas se descartaron.
    // Nunca lanza excepciones: las líneas mal formadas solo se cuentan.
    fun parsear(texto: String): Pair<List<FilaCsv>, Int> {
        val filas = mutableListOf<FilaCsv>()
        var invalidos = 0

        texto.lineSequence().forEach { lineaCruda ->
            val linea = lineaCruda.removePrefix("\uFEFF").trim()
            if (linea.isEmpty()) return@forEach
            // Cabecera (también si viene repetida o con otras mayúsculas).
            if (linea.equals(CABECERA, ignoreCase = true)) return@forEach

            val partes = linea.split(",", limit = 2)
            if (partes.size != 2) {
                invalidos++
                return@forEach
            }

            val tipo = TipoCsv.desde(partes[0].trim().lowercase())
            if (tipo == null) {
                invalidos++
                return@forEach
            }

            val valor = ReglaBloqueo.normalizar(partes[1].trim())
            if (valor.isEmpty() || valor.none { it.isDigit() }) {
                invalidos++
                return@forEach
            }

            filas.add(FilaCsv(tipo, valor))
        }

        return filas to invalidos
    }

    // Evita que un valor guardado rompa el formato del CSV.
    private fun limpiarCampo(valor: String): String =
        valor.replace(",", "").replace("\n", "").replace("\r", "").trim()
}
