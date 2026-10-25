package com.example.filtro400

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvListasTest {

    @Test
    fun `parsea cabecera y filas validas`() {
        val (filas, invalidos) = CsvListas.parsear(
            """
            tipo,valor
            bloqueado,612345678
            blanco,600111222
            prefijo,400
            """.trimIndent()
        )

        assertEquals(0, invalidos)
        assertEquals(
            listOf(
                FilaCsv(TipoCsv.BLOQUEADO, "612345678"),
                FilaCsv(TipoCsv.BLANCO, "600111222"),
                FilaCsv(TipoCsv.PREFIJO, "400")
            ),
            filas
        )
    }

    @Test
    fun `ignora lineas vacias y cabeceras repetidas`() {
        val (filas, invalidos) = CsvListas.parsear(
            "tipo,valor\n\n  \nbloqueado,612345678\nTIPO,VALOR\nblanco,600111222\n"
        )

        assertEquals(0, invalidos)
        assertEquals(
            listOf(
                FilaCsv(TipoCsv.BLOQUEADO, "612345678"),
                FilaCsv(TipoCsv.BLANCO, "600111222")
            ),
            filas
        )
    }

    @Test
    fun `las lineas mal formadas se cuentan como invalidas sin lanzar`() {
        val (filas, invalidos) = CsvListas.parsear(
            """
            tipo,valor
            bloqueado
            raro,123
            blanco,
            prefijo
            ,400
            bloqueado,abc
            """.trimIndent()
        )

        assertEquals(6, invalidos)
        assertTrue(filas.isEmpty())
    }

    @Test
    fun `normaliza numeros y prefijos`() {
        val (filas, invalidos) = CsvListas.parsear(
            """
            tipo,valor
            bloqueado,+34 612-345-678
            blanco,0034 600 111 222
            prefijo, 400 
            """.trimIndent()
        )

        assertEquals(0, invalidos)
        assertEquals(
            listOf(
                FilaCsv(TipoCsv.BLOQUEADO, "612345678"),
                FilaCsv(TipoCsv.BLANCO, "600111222"),
                FilaCsv(TipoCsv.PREFIJO, "400")
            ),
            filas
        )
    }

    @Test
    fun `tolera BOM y saltos de linea de Windows`() {
        val (filas, invalidos) = CsvListas.parsear(
            "\uFEFFtipo,valor\r\nbloqueado,612345678\r\nprefijo,400\r\n"
        )

        assertEquals(0, invalidos)
        assertEquals(2, filas.size)
    }

    @Test
    fun `genera el CSV con cabecera y todos los tipos`() {
        val csv = CsvListas.generarCsv(
            prefijos = listOf("400"),
            bloqueados = listOf("612345678"),
            blancos = listOf("600111222")
        )

        val lineas = csv.lines().filter { it.isNotBlank() }
        assertEquals(4, lineas.size)
        assertEquals(CsvListas.CABECERA, lineas[0])
        assertEquals("bloqueado,612345678", lineas[1])
        assertEquals("blanco,600111222", lineas[2])
        assertEquals("prefijo,400", lineas[3])
    }

    @Test
    fun `genera solo la cabecera cuando no hay datos`() {
        val csv = CsvListas.generarCsv(emptyList(), emptyList(), emptyList())
        assertEquals(CsvListas.CABECERA, csv.trim())
    }
}
