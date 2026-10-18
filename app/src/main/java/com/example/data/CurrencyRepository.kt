package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CurrencyInfo(
    val code: String,
    val nameAr: String,
    val flag: String,
    val symbol: String
)

class CurrencyRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alhasiba_currency", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    val supportedCurrencies = listOf(
        CurrencyInfo("SAR", "ريال سعودي", "🇸🇦", "ر.س"),
        CurrencyInfo("EGP", "جنيه مصري", "🇪🇬", "ج.م"),
        CurrencyInfo("AED", "درهم إماراتي", "🇦🇪", "د.إ"),
        CurrencyInfo("KWD", "دينار كويتي", "🇰🇼", "د.ك"),
        CurrencyInfo("QAR", "ريال قطري", "🇶🇦", "ر.ق"),
        CurrencyInfo("BHD", "دينار بحريني", "🇧🇭", "د.ب"),
        CurrencyInfo("OMR", "ريال عماني", "🇴🇲", "ر.ع"),
        CurrencyInfo("JOD", "دينار أردني", "🇯🇴", "د.أ"),
        CurrencyInfo("MAD", "درهم مغربي", "🇲🇦", "د.م"),
        CurrencyInfo("DZD", "دينار جزائري", "🇩🇿", "د.ج"),
        CurrencyInfo("IQD", "دينار عراقي", "🇮🇶", "د.ع"),
        CurrencyInfo("LYD", "دينار ليبي", "🇱🇾", "د.ل"),
        CurrencyInfo("USD", "دولار أمريكي", "🇺🇸", "$"),
        CurrencyInfo("EUR", "يورو أوروبي", "🇪🇺", "€"),
        CurrencyInfo("GBP", "جنيه إسترليني", "🇬🇧", "£"),
        CurrencyInfo("JPY", "ين ياباني", "🇯🇵", "¥"),
        CurrencyInfo("CAD", "دولار كندي", "🇨🇦", "C$"),
        CurrencyInfo("AUD", "دولار أسترالي", "🇦🇺", "A$"),
        CurrencyInfo("CHF", "فرنك سويسري", "🇨🇭", "CHF"),
        CurrencyInfo("CNY", "يوان صيني", "🇨🇳", "¥"),
        CurrencyInfo("TRY", "ليرة تركية", "🇹🇷", "₺"),
        CurrencyInfo("INR", "روبية هندية", "🇮🇳", "₹")
    )

    // Base currency for storage is USD
    private val defaultRatesToUsd = mapOf(
        "USD" to 1.0,
        "SAR" to 3.75,
        "EGP" to 48.65,
        "AED" to 3.6725,
        "KWD" to 0.3075,
        "QAR" to 3.64,
        "BHD" to 0.376,
        "OMR" to 0.385,
        "JOD" to 0.709,
        "MAD" to 9.85,
        "DZD" to 134.5,
        "IQD" to 1310.0,
        "LYD" to 4.86,
        "EUR" to 0.925,
        "GBP" to 0.772,
        "JPY" to 152.4,
        "CAD" to 1.385,
        "AUD" to 1.512,
        "CHF" to 0.865,
        "CNY" to 7.12,
        "TRY" to 34.25,
        "INR" to 84.1
    )

    private val _rates = MutableStateFlow<Map<String, Double>>(loadCachedRates())
    val rates: StateFlow<Map<String, Double>> = _rates.asStateFlow()

    private val _lastUpdated = MutableStateFlow(prefs.getString("last_updated_str", "أسعار افتراضية محفوظة") ?: "أسعار افتراضية محفوظة")
    val lastUpdated: StateFlow<String> = _lastUpdated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private fun loadCachedRates(): Map<String, Double> {
        val result = defaultRatesToUsd.toMutableMap()
        val jsonStr = prefs.getString("rates_json", null)
        if (!jsonStr.isNullOrBlank()) {
            runCatching {
                val json = JSONObject(jsonStr)
                for (code in defaultRatesToUsd.keys) {
                    if (json.has(code)) {
                        result[code] = json.getDouble(code)
                    }
                }
            }
        }
        return result
    }

    suspend fun refreshRates(): Boolean = withContext(Dispatchers.IO) {
        _isLoading.value = true
        var success = false
        try {
            val request = Request.Builder()
                .url("https://open.er-api.com/v6/latest/USD")
                .header("User-Agent", "AlHasiba-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val ratesObj = json.optJSONObject("rates")
                        if (ratesObj != null) {
                            val newRates = _rates.value.toMutableMap()
                            for (cur in supportedCurrencies) {
                                if (ratesObj.has(cur.code)) {
                                    newRates[cur.code] = ratesObj.getDouble(cur.code)
                                }
                            }
                            // Save to prefs
                            val toSave = JSONObject()
                            newRates.forEach { (k, v) -> toSave.put(k, v) }
                            val dateStr = "تم التحديث عبر الإنترنت"
                            prefs.edit()
                                .putString("rates_json", toSave.toString())
                                .putString("last_updated_str", dateStr)
                                .apply()

                            _rates.value = newRates
                            _lastUpdated.value = dateStr
                            success = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fallback to cached/default rates
            success = false
        } finally {
            _isLoading.value = false
        }
        success
    }

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        val currentRates = _rates.value
        val fromRate = currentRates[fromCode] ?: defaultRatesToUsd[fromCode] ?: 1.0
        val toRate = currentRates[toCode] ?: defaultRatesToUsd[toCode] ?: 1.0

        // from USD: amount / fromRate -> amount in USD; then * toRate -> amount in toCode
        val inUsd = amount / fromRate
        return inUsd * toRate
    }
}
