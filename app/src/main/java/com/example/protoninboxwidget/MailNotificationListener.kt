package com.example.protoninboxwidget

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MailNotificationListener : NotificationListenerService() {

    companion object {
        const val PROTON_PACKAGE = "ch.protonmail.android"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        DebugLog.add(applicationContext, "Listener connected ✅")
        try {
            activeNotifications
                ?.filter { it.packageName == PROTON_PACKAGE }
                ?.sortedBy { it.postTime }
                ?.forEach { handle(it) }
        } catch (e: Exception) {
            DebugLog.add(applicationContext, "Error reading active notifications: ${e.message}")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != PROTON_PACKAGE) return
        handle(sbn)
    }

    private fun handle(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        val extras = n.extras
        val ctx = applicationContext

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString()?.trim()
        val isSummary = n.flags and Notification.FLAG_GROUP_SUMMARY != 0

        DebugLog.add(
            ctx,
            "Proton notif: summary=$isSummary title='${title ?: ""}' text='${(text ?: "").take(60)}' keys=${extras.keySet().joinToString(",")}"
        )

        var added = 0

        // 1) MessagingStyle: individual messages in EXTRA_MESSAGES
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages != null && messages.isNotEmpty()) {
            messages.forEach { p ->
                val b = p as? Bundle ?: return@forEach
                val sender = (b.getCharSequence("sender_person")?.toString()
                    ?: b.getCharSequence("sender")?.toString())?.trim()
                val msgText = b.getCharSequence("text")?.toString()?.trim()
                val time = b.getLong("time", sbn.postTime)
                if (!msgText.isNullOrBlank()) {
                    MailStore.add(ctx, MailItem(sender ?: (title ?: "Proton Mail"), msgText, time))
                    added++
                }
            }
        }

        // 2) InboxStyle: lines in EXTRA_TEXT_LINES ("Sender  Subject" per line)
        if (added == 0) {
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { line ->
                val s = line?.toString()?.trim() ?: return@forEach
                if (s.isNotBlank()) {
                    MailStore.add(ctx, MailItem(title ?: "Proton Mail", s, sbn.postTime))
                    added++
                }
            }
        }

        // 3) Plain title/text notification (skip pure group summaries here)
        if (added == 0 && !isSummary && !title.isNullOrBlank() && !text.isNullOrBlank()) {
            val lower = text.lowercase()
            val service = lower.contains("fetching") || lower.contains("sending message") ||
                lower.contains("logged out") || lower.contains("new message", ignoreCase = false) &&
                Regex("^\\d+ new message").containsMatchIn(lower)
            if (!service) {
                MailStore.add(ctx, MailItem(title, text, sbn.postTime))
                added++
            } else {
                DebugLog.add(ctx, "Skipped as service/summary text")
            }
        }

        DebugLog.add(ctx, "→ stored $added item(s)")
    }
}
