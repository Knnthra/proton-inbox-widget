package com.example.protoninboxwidget

import android.content.Context
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tiny ring buffer of diagnostic messages, viewable in MainActivity. */
object DebugLog {

    private const val PREFS = "debug_log"
    private const val KEY = "lines"
    private const val MAX = 40

    fun add(context: Context, message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val lines = getAll(context).toMutableList()
        lines.add(0, "[$time] $message")
        while (lines.size > MAX) lines.removeAt(lines.size - 1)
        val arr = JSONArray()
        lines.forEach { arr.put(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun getAll(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY).apply()
    }
}
