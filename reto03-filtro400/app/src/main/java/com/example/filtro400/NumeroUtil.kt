package com.example.filtro400

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import java.util.Locale

// Número ya interpretado por libphonenumber.
data class NumeroNormalizado(
    val e164: String,
    val codigoPais: String,
    val nacional: String
)

// País que puede elegirse como región por defecto.
data class Pais(
    val iso: String,
    val nombre: String,
    val codigo: String,
    val bandera: String
)

object NumeroUtil {

    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    // Devuelve null si el texto no se puede interpretar como número.
    // No se usa isValidNumber a propósito: basta con que parse funcione.
    fun normalizar(numero: String, regionPorDefecto: String): NumeroNormalizado? {
        return try {
            val telefono = util.parse(numero, regionPorDefecto)
            NumeroNormalizado(
                e164 = util.format(telefono, PhoneNumberFormat.E164),
                codigoPais = telefono.countryCode.toString(),
                nacional = util.getNationalSignificantNumber(telefono)
            )
        } catch (e: NumberParseException) {
            null
        }
    }

    // Regiones que conoce libphonenumber, ordenadas por nombre.
    fun paises(): List<Pais> {
        return util.supportedRegions.mapNotNull { iso ->
            val codigo = util.getCountryCodeForRegion(iso)
            if (codigo == 0) {
                null
            } else {
                Pais(
                    iso = iso,
                    nombre = Locale("", iso).getDisplayCountry(),
                    codigo = codigo.toString(),
                    bandera = bandera(iso)
                )
            }
        }.sortedBy { it.nombre }
    }

    // Única función que depende de Android.
    fun regionDelDispositivo(context: Context): String {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val sim = telephony?.simCountryIso
        val red = telephony?.networkCountryIso

        val region = when {
            !sim.isNullOrBlank() -> sim
            !red.isNullOrBlank() -> red
            else -> Locale.getDefault().country
        }

        return if (region.isBlank()) "ES" else region.uppercase(Locale.ROOT)
    }

    // Bandera emoji a partir de las dos letras del código ISO.
    private fun bandera(iso: String): String {
        if (iso.length != 2) return ""
        val base = 0x1F1E6 // 🇦
        return buildString {
            iso.uppercase(Locale.ROOT).forEach { letra ->
                if (letra in 'A'..'Z') {
                    append(String(Character.toChars(base + (letra - 'A'))))
                }
            }
        }
    }
}
