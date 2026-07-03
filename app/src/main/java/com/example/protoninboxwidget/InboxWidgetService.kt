package com.example.protoninboxwidget

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class InboxWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        InboxRemoteViewsFactory(applicationContext)
}

class InboxRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<MailItem> = emptyList()

    override fun onCreate() { reload() }
    override fun onDataSetChanged() { reload() }
    override fun onDestroy() { items = emptyList() }

    private fun reload() {
        items = MailStore.getAll(context)
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        return RemoteViews(context.packageName, R.layout.widget_item).apply {
            setTextViewText(R.id.item_sender, item.sender)
            setTextViewText(R.id.item_subject, item.subject)
            setTextViewText(
                R.id.item_time,
                DateUtils.getRelativeTimeSpanString(
                    item.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
            )
            // Fill-in intent so taps fire the provider's template
            setOnClickFillInIntent(
                R.id.item_root,
                Intent().putExtra("item_id", item.id)
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = false
}
