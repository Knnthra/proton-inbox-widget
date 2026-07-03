package com.example.protoninboxwidget

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MailNotificationListener : NotificationListenerService() {

    companion object {
        const val PROTON_PACKAGE = "ch.protonmail.android"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Pick up any Proton notifications already in the shade
        try {
            activeNotifications
                ?.filter { it.packageName == PROTON_PACKAGE }
                ?.sortedBy { it.postTime }
                ?.forEach { handle(it) }
        } catch (e: Exception) {
            // activeNotifications can throw if listener isn't fully bound yet
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != PROTON_PACKAGE) return
        handle(sbn)
    }

    private fun handle(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return

        // Skip the "N new messages" group summary — we want individual mails
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val extras = n.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
            ?.toString()?.trim()

        // Proton mail notifications: title = sender, text = subject.
        // Ignore service notifications like "Fetching new emails".
        if (title.isNullOrBlank() || text.isNullOrBlank()) return
        val lower = text.lowercase()
        if (lower.contains("fetching") || lower.contains("sending message") ||
            lower.contains("logged out")) return

        MailStore.add(
            applicationContext,
            MailItem(sender = title, subject = text, timestamp = sbn.postTime)
        )
    }
}
