package com.example.filtro400

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglaBloqueoTest {

    private val prefijosFase1 = listOf("400")

    @Test fun bloquea400SinPrefijoPais() =
        assertTrue(ReglaBloqueo.debeBloquearse("400123456", prefijosFase1))

    @Test fun bloquea400ConMas34() =
        assertTrue(ReglaBloqueo.debeBloquearse("+34400123456", prefijosFase1))

    @Test fun bloquea400Con0034() =
        assertTrue(ReglaBloqueo.debeBloquearse("0034400123456", prefijosFase1))

    @Test fun bloquea400ConEspacios() =
        assertTrue(ReglaBloqueo.debeBloquearse("+34 400 123 456", prefijosFase1))

    @Test fun noBloqueaMovil() =
        assertFalse(ReglaBloqueo.debeBloquearse("612345678", prefijosFase1))

    @Test fun noBloqueaFijo() =
        assertFalse(ReglaBloqueo.debeBloquearse("912345678", prefijosFase1))

    @Test fun noBloqueaInternacionalQueContiene400() =
        assertFalse(ReglaBloqueo.debeBloquearse("+1400123456", prefijosFase1))

    @Test fun noBloqueaNumeroVacio() =
        assertFalse(ReglaBloqueo.debeBloquearse("", prefijosFase1))

    // Varios prefijos: cualquiera de ellos puede bloquear.
    @Test fun bloqueaConVariosPrefijos() {
        val prefijos = listOf("400", "900")
        assertTrue(ReglaBloqueo.debeBloquearse("400123456", prefijos))
        assertTrue(ReglaBloqueo.debeBloquearse("900123456", prefijos))
        assertFalse(ReglaBloqueo.debeBloquearse("612345678", prefijos))
    }

    // Sin prefijos configurados no se bloquea nada.
    @Test fun noBloqueaConListaVacia() =
        assertFalse(ReglaBloqueo.debeBloquearse("400123456", emptyList()))

    // Un prefijo vacío (o sin dígitos) nunca debe bloquear.
    @Test fun prefijoVacioNuncaBloquea() {
        assertFalse(ReglaBloqueo.debeBloquearse("400123456", listOf("")))
        assertFalse(ReglaBloqueo.debeBloquearse("612345678", listOf("")))
        assertFalse(ReglaBloqueo.debeBloquearse("612345678", listOf("400", "")))
    }

    // El prefijo también se normaliza quedándose solo con sus dígitos.
    @Test fun prefijoConFormatoSoloUsaDigitos() =
        assertTrue(ReglaBloqueo.debeBloquearse("400123456", listOf("4 0 0")))

    @Test fun bloqueaNumeroConMas34YVariosPrefijos() =
        assertTrue(ReglaBloqueo.debeBloquearse("+34900123456", listOf("400", "900")))

    @Test fun bloqueaNumeroCon0034YVariosPrefijos() =
        assertTrue(ReglaBloqueo.debeBloquearse("0034900123456", listOf("400", "900")))

    // Número que no coincide con ningún prefijo.
    @Test fun noBloqueaNumeroQueNoCoincide() =
        assertFalse(ReglaBloqueo.debeBloquearse("666123456", listOf("400", "900")))
}
