package com.example.filtro400

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NumeroUtilTest {

    private fun assertNormalizado(numero: String) {
        val resultado = NumeroUtil.normalizar(numero, "ES")
        assertNotNull("No se pudo normalizar: $numero", resultado)
        assertEquals("+34400123456", resultado!!.e164)
        assertEquals("34", resultado.codigoPais)
        assertEquals("400123456", resultado.nacional)
    }

    @Test
    fun `normaliza formato internacional`() {
        assertNormalizado("+34400123456")
    }

    @Test
    fun `normaliza prefijo internacional 00`() {
        assertNormalizado("0034400123456")
    }

    @Test
    fun `normaliza numero nacional con region ES`() {
        assertNormalizado("400123456")
    }

    @Test
    fun `normaliza numero con espacios`() {
        assertNormalizado("+34 400 123 456")
    }

    @Test
    fun `conserva el codigo de pais 1`() {
        val resultado = NumeroUtil.normalizar("+1400123456", "ES")
        assertNotNull(resultado)
        assertEquals("+1400123456", resultado!!.e164)
        assertEquals("1", resultado.codigoPais)
    }

    @Test
    fun `texto invalido devuelve null`() {
        assertNull(NumeroUtil.normalizar("esto no es un número", "ES"))
    }

    @Test
    fun `paises contiene Espana con codigo 34`() {
        val paises = NumeroUtil.paises()
        assertTrue(paises.isNotEmpty())

        val espana = paises.firstOrNull { it.iso == "ES" }
        assertNotNull("No aparece España en paises()", espana)
        assertEquals("34", espana!!.codigo)
    }
}
