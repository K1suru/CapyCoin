package com.example.gestiongastos.data.model

// El dinero se guarda en centavos enteros (Long), tanto pesos como dólares, para que la base de datos
// sea exacta. Las tasas (interés, TRM) y los porcentajes siguen siendo Double.

/** Mayor monto que acepta la app, en centavos (igual que MAX_AMOUNT en pesos). */
const val MAX_AMOUNT_CENTS = 999_999_999_999_900L

/**
 * Convierte un monto a centavos, redondeando al centavo más cercano (la mitad hacia arriba).
 * Lanza IllegalArgumentException si el monto no es finito o no cabe: nunca guarda un valor inventado.
 */
fun toCents(amount: Double): Long {
    require(amount.isFinite()) { "Monto no válido: $amount" }
    require(amount >= -MAX_AMOUNT_CENTS / 100.0 && amount <= MAX_AMOUNT_CENTS / 100.0) { "Monto fuera de rango: $amount" }
    return Math.round(amount * 100.0)
}

/** Igual que [toCents] pero devuelve null en vez de lanzar (para datos que vienen de una copia de seguridad). */
fun toCentsOrNull(amount: Double): Long? = try {
    toCents(amount)
} catch (e: IllegalArgumentException) {
    null
}

/** Centavos a monto con decimales, para cálculos y para mostrar. Exacto hasta [MAX_AMOUNT_CENTS]. */
fun centsToAmount(cents: Long): Double = cents / 100.0

/** Redondea un monto al centavo más cercano y lo devuelve con decimales. */
fun roundToCents(amount: Double): Double = centsToAmount(toCents(amount))

/** Baja un monto al centavo (33,335 → 33,33). El margen evita que 33,33 se lea como 33,3299… y baje a 33,32. */
fun floorToCents(amount: Double): Double = Math.floor(amount * 100.0 + 1e-6) / 100.0

/**
 * Valor de una cuota del plan en centavos exactos. Todas las cuotas se bajan al centavo y la última
 * ([isLast]) paga todo lo que queda, así absorbe el residuo y el plan suma exactamente lo debido.
 */
fun installmentInCents(balance: Double, rate: Double, remainingInstallments: Int, isLast: Boolean): Double =
    if (isLast) roundToCents(balance * (1.0 + rate)) else floorToCents(frenchPayment(balance, rate, remainingInstallments))
