package com.example.protoninboxwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MailItem(
    val sender: String,
    val subject: String,
    val timestamp: Long,
    val id: String = ""
)

/**
 * Tiny persistent store backed by SharedPreferences.
 * Keeps the most recent [MAX_ITEMS] mail notifications.
 */
object MailStore {

    private const val PREFS = "mail_store"
    private const val KEY = "items"
    private const val MAX_ITEMS = 30

    fun add(context: Context, item: MailItem) {
        val items = getAll(context).toMutableList()

        // De-dupe: same sender + subject within 2 minutes is the same mail
        val duplicate = items.any {
            it.sender == item.sender &&
            it.subject == item.subject &&
            Math.abs(it.timestamp - item.timestamp) < 120_000
        }
        if (duplicate) return

        items.add(0, item)
        while (items.size > MAX_ITEMS) items.removeAt(items.size - 1)
        save(context, items)
        notifyWidgets(context)
    }

    fun getAll(context: Context): List<MailItem> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MailItem(
                    o.optString("sender"),
                    o.optString("subject"),
                    o.optLong("timestamp"),
                    o.optString("id")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        save(context, emptyList())
        notifyWidgets(context)
    }

    private fun save(context: Context, items: List<MailItem>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(JSONObject().apply {
                put("sender", it.sender)
                put("subject", it.subject)
                put("timestamp", it.timestamp)
                put("id", it.id)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, arr.toString())
            .apply()
    }

    private fun notifyWidgets(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(
            ComponentName(context, InboxWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list)
            InboxWidgetProvider.updateAll(context, mgr, ids)
        }
    }
}
