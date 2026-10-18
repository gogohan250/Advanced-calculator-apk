package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CalculationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alhasiba_history", Context.MODE_PRIVATE)

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val jsonString = prefs.getString("history_items", "[]") ?: "[]"
        val list = mutableListOf<CalculationHistoryItem>()
        runCatching {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }
        _history.value = list
    }

    private fun persistHistory(items: List<CalculationHistoryItem>) {
        val jsonArray = JSONArray()
        items.take(100).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString("history_items", jsonArray.toString()).apply()
        _history.value = items
    }

    fun addHistory(expression: String, result: String) {
        if (expression.isBlank() || result.isBlank() || result == "خطأ") return
        val newItem = CalculationHistoryItem(expression = expression, result = result)
        val current = _history.value.toMutableList()
        current.add(0, newItem)
        persistHistory(current)
    }

    fun deleteItem(id: String) {
        val current = _history.value.filter { it.id != id }
        persistHistory(current)
    }

    fun clearAll() {
        persistHistory(emptyList())
    }
}
