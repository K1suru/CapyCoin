package com.example.gestiongastos.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
     * - Returns null if it cannot be fetched (sin conexión o respuesta inválida); el llamador decide el respaldo.
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
                return@withContext cached
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(raw)
            if (jsonArray.length() > 0) {
                val entry = jsonArray.getJSONObject(0)
                val rate = entry.getString("valor").replace(",", ".").toDouble()
                if (rate <= 0.0) return@withContext cached
                cachedDate = today
                cachedRate = rate
                Log.d(TAG, "TRM Banco de la República: $$rate COP/USD (vigencia: ${entry.optString("vigenciadesde", "?")})")
                rate
            } else {
                Log.w(TAG, "TRM API returned empty array")
                cached
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch TRM", e)
            cached
        } finally {
            connection?.disconnect()
        }
    }
}
