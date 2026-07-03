package com.example.protoninboxwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class InboxWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_CLEAR = "com.example.protoninboxwidget.ACTION_CLEAR"
        const val ACTION_ITEM_CLICK = "com.example.protoninboxwidget.ACTION_ITEM_CLICK"

        fun updateAll(context: Context, mgr: AppWidgetManager, ids: IntArray) {
            ids.forEach { updateWidget(context, mgr, it) }
        }

        private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_inbox)

            val count = MailStore.getAll(context).size
            views.setTextViewText(
                R.id.widget_title,
                if (count > 0) "Proton Mail ($count)" else "Proton Mail"
            )

            // List adapter
            val svcIntent = Intent(context, InboxWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, svcIntent)
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // Tapping the header opens Proton Mail
            val launch = context.packageManager
                .getLaunchIntentForPackage(MailNotificationListener.PROTON_PACKAGE)
            if (launch != null) {
                views.setOnClickPendingIntent(
                    R.id.widget_title,
                    PendingIntent.getActivity(
                        context, 0, launch,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            // Clear button
            val clearIntent = Intent(context, InboxWidgetProvider::class.java)
                .setAction(ACTION_CLEAR)
            views.setOnClickPendingIntent(
                R.id.widget_clear,
                PendingIntent.getBroadcast(
                    context, 1, clearIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Template for row taps (fired by the RemoteViewsFactory fill-ins)
            val itemIntent = Intent(context, InboxWidgetProvider::class.java)
                .setAction(ACTION_ITEM_CLICK)
            views.setPendingIntentTemplate(
                R.id.widget_list,
                PendingIntent.getBroadcast(
                    context, 2, itemIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )

            mgr.updateAppWidget(widgetId, views)
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        updateAll(context, mgr, ids)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_CLEAR -> MailStore.clear(context)
            ACTION_ITEM_CLICK -> {
                // Open the Proton Mail app (deep-linking to a specific mail
                // isn't possible from outside the app)
                context.packageManager
                    .getLaunchIntentForPackage(MailNotificationListener.PROTON_PACKAGE)
                    ?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
            }
        }
    }
}
