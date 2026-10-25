package com.example.filtro400

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorFiltradoTest {

    private fun reglas(
        listaBlanca: List<String> = emptyList(),
        numerosBloqueados: List<String> = emptyList(),
        prefijos: List<PrefijoRegla> = emptyList()
    ) = ReglasActivas(listaBlanca, numerosBloqueados, prefijos)

    private fun prefijo(codigoPais: String, prefijo: String, prioridad: Int = 0) =
        PrefijoRegla(codigoPais, prefijo, prioridad)

    // Un 400 español y el mismo 400 francés (mismo prefijo, distinto país).
    private val espanol400 = NumeroNormalizado("+34400123456", "34", "400123456")
    private val frances400 = NumeroNormalizado("+33400123456", "33", "400123456")
    private val espanol600 = NumeroNormalizado("+34600111222", "34", "600111222")

    // La lista blanca gana aunque el número coincida con un prefijo.
    @Test fun listaBlancaGanaAPrefijo() {
        val decision = MotorFiltrado.evaluar(
            espanol400,
            reglas(
                listaBlanca = listOf("+34400123456"),
                prefijos = listOf(prefijo("34", "400", 10))
            )
        )
        assertFalse(decision.bloquear)
        assertEquals("lista_blanca", decision.regla)
        assertEquals("+34400123456", decision.detalle)
    }

    // La lista blanca gana aunque el número esté bloqueado explícitamente.
    @Test fun listaBlancaGanaANumeroBloqueado() {
        val numero = NumeroNormalizado("+34612345678", "34", "612345678")
        val decision = MotorFiltrado.evaluar(
            numero,
            reglas(
                listaBlanca = listOf("+34612345678"),
                numerosBloqueados = listOf("+34612345678")
            )
        )
        assertFalse(decision.bloquear)
        assertEquals("lista_blanca", decision.regla)
        assertEquals("+34612345678", decision.detalle)
    }

    // Los números bloqueados se comparan por igualdad exacta en E.164.
    @Test fun numeroBloqueadoExplicitamente() {
        val numero = NumeroNormalizado("+34900123456", "34", "900123456")
        val decision = MotorFiltrado.evaluar(
            numero,
            reglas(numerosBloqueados = listOf("+34900123456"))
        )
        assertTrue(decision.bloquear)
        assertEquals("numero", decision.regla)
        assertEquals("+34900123456", decision.detalle)
    }

    // El mismo prefijo puede existir en dos países sin cruzarse.
    @Test fun mismoPrefijoEnDosPaises() {
        val soloEspana = reglas(prefijos = listOf(prefijo("34", "400")))
        assertTrue(MotorFiltrado.evaluar(espanol400, soloEspana).bloquear)
        assertFalse(MotorFiltrado.evaluar(frances400, soloEspana).bloquear)

        val ambosPaises = reglas(
            prefijos = listOf(prefijo("34", "400"), prefijo("33", "400"))
        )
        assertTrue(MotorFiltrado.evaluar(espanol400, ambosPaises).bloquear)
        assertTrue(MotorFiltrado.evaluar(frances400, ambosPaises).bloquear)
    }

    // Un prefijo sin código de país se aplica al número nacional de cualquiera.
    @Test fun prefijoDeCualquierPais() {
        val cualquierPais = reglas(prefijos = listOf(prefijo("", "400")))

        assertTrue(MotorFiltrado.evaluar(espanol400, cualquierPais).bloquear)
        assertTrue(MotorFiltrado.evaluar(frances400, cualquierPais).bloquear)
        assertFalse(MotorFiltrado.evaluar(espanol600, cualquierPais).bloquear)
    }

    // Entre prefijos que coinciden gana el de mayor prioridad.
    @Test fun prioridadEntrePrefijos() {
        val decision = MotorFiltrado.evaluar(
            espanol400,
            reglas(
                prefijos = listOf(
                    prefijo("34", "400", 1),
                    prefijo("34", "4001", 10)
                )
            )
        )
        assertTrue(decision.bloquear)
        assertEquals("prefijo", decision.regla)
        assertEquals("4001", decision.detalle)
    }

    // Un número normalizado vacío nunca se bloquea.
    @Test fun numeroVacioNoBloquea() {
        val decision = MotorFiltrado.evaluar(
            NumeroNormalizado("", "", ""),
            reglas(
                numerosBloqueados = listOf("+34400123456"),
                prefijos = listOf(prefijo("34", "400"))
            )
        )
        assertFalse(decision.bloquear)
        assertEquals("ninguna", decision.regla)
        assertEquals(null, decision.detalle)
    }

    @Test fun enmascararDejaTresPrimerosYTresUltimos() {
        assertEquals("400***456", MotorFiltrado.enmascarar("400123456"))
        assertEquals("123*567", MotorFiltrado.enmascarar("1234567"))
        assertEquals("344*****456", MotorFiltrado.enmascarar("+34 400 123 456"))
    }

    @Test fun enmascararNumerosCortos() {
        assertEquals("***", MotorFiltrado.enmascarar("123456"))
        assertEquals("***", MotorFiltrado.enmascarar("12345"))
        assertEquals("***", MotorFiltrado.enmascarar(""))
    }
}
