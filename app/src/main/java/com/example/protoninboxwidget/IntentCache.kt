package com.example.protoninboxwidget

import android.app.PendingIntent
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory cache of the "open this email" PendingIntents captured from
 * Proton Mail notifications. Lost on process death — taps then fall back
 * to opening the Proton Mail app.
 */
object IntentCache {
    private val map = ConcurrentHashMap<String, PendingIntent>()

    fun put(id: String, pi: PendingIntent) {
        if (map.size > 60) map.clear()
        map[id] = pi
    }

    fun get(id: String): PendingIntent? = map[id]
}
