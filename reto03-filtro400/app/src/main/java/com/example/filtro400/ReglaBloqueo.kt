package com.example.filtro400

// "object" = una única instancia, sin necesidad de crearla con ReglaBloqueo().
// Aquí vive la decisión de bloquear o no: sin Android, sin pantalla.
object ReglaBloqueo {

    // Un mismo número puede llegar de varias formas:
    // "400123456", "+34400123456", "0034 400 123 456", "+34 400 123 456"...
    // Lo dejamos siempre en el mismo formato antes de comparar.
    fun normalizar(numero: String): String {
        // Nos quedamos solo con dígitos y el signo "+" (fuera espacios y guiones).
        val limpio = numero.filter { it.isDigit() || it == '+' }
        return when {
            limpio.startsWith("+34") -> limpio.removePrefix("+34")
            limpio.startsWith("0034") -> limpio.removePrefix("0034")
            else -> limpio
        }
    }

    // La regla: bloquear si el número empieza por alguno de los prefijos activos.
    fun debeBloquearse(numero: String, prefijos: List<String>): Boolean {
        val normalizado = normalizar(numero)
        if (normalizado.isEmpty()) return false

        return prefijos.any { prefijo ->
            // De cada prefijo nos quedamos solo con sus dígitos.
            val digitos = prefijo.filter { it.isDigit() }
            // Un prefijo vacío nunca debe bloquear.
            digitos.isNotEmpty() && normalizado.startsWith(digitos)
        }
    }
}