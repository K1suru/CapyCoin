package com.example.gestiongastos.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Rango en el que una TRM (pesos por dólar) es creíble; fuera de él se descarta la respuesta. */
val PLAUSIBLE_USD_RATE = 500.0..50_000.0

/**
 * Lee el valor de la TRM que devuelve la API ("4150.25" o "4150,25").
 * Devuelve null si no es un número finito dentro de [PLAUSIBLE_USD_RATE]: una tasa NaN, 0 o disparatada
 * dañaría todos los montos en dólares.
 */
fun parseTrmValue(text: String): Double? =
    text.trim().replace(",", ".").toDoubleOrNull()?.takeIf { it.isFinite() && it in PLAUSIBLE_USD_RATE }

object ExchangeRateService {
    private const val TAG = "ExchangeRateService"

    // datos.gov.co — dataset de TRM oficial del Banco de la República de Colombia
    // Returns JSON with field "valor" for the COP per USD rate
    private const val API_URL =
        "https://www.datos.gov.co/resource/mcec-87by.json?\$limit=1&\$order=vigenciadesde+DESC"

    // Formatter to display in logs
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // In-memory cache: (date → rate)
    @Volatile private var cachedDate: LocalDate? = null
    @Volatile private var cachedRate: Double? = null

    /**
     * Returns the current TRM (Tasa Representativa del Mercado) USD → COP from Banco de la República.
     * - Fetches once per calendar day; subsequent calls return the cached value.
     * - Devuelve null si hoy no se pudo obtener (sin conexión o respuesta inválida). Ya no devuelve en
     *   silencio la tasa de un día anterior: el llamador conserva la última conocida y sabe que no es la de hoy.
     */
    suspend fun getUsdToCopRate(): Double? = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val cached = cachedRate
        if (cachedDate == today && cached != null) {
            Log.d(TAG, "TRM from cache: $cached (date: ${today.format(dateFormatter)})")
            return@withContext cached
        }

        var connection: HttpURLConnection? = null
        try {
            connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            if (connection.responseCode !in 200..299) {
                Log.w(TAG, "TRM API HTTP ${connection.responseCode}")
                return@withContext null
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(raw)
            if (jsonArray.length() > 0) {
                val entry = jsonArray.getJSONObject(0)
                val rate = parseTrmValue(entry.getString("valor")) ?: run {
                    Log.w(TAG, "TRM API returned an invalid rate")
                    return@withContext null
                }
                cachedDate = today
                cachedRate = rate
                Log.d(TAG, "TRM Banco de la República: $$rate COP/USD (vigencia: ${entry.optString("vigenciadesde", "?")})")
                rate
            } else {
                Log.w(TAG, "TRM API returned empty array")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch TRM", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}
