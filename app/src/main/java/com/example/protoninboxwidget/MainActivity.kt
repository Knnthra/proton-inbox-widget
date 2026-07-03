package com.example.protoninboxwidget

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_grant).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            MailStore.add(
                this,
                MailItem("Test sender", "If you can see this on the widget, the widget works!", System.currentTimeMillis())
            )
            refreshLog()
        }

        findViewById<Button>(R.id.btn_refresh_log).setOnClickListener { refreshLog() }
        findViewById<Button>(R.id.btn_clear_log).setOnClickListener {
            DebugLog.clear(this)
            refreshLog()
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = Settings.Secure
            .getString(contentResolver, "enabled_notification_listeners")
            ?.contains(packageName) == true

        findViewById<TextView>(R.id.status).text = if (enabled) {
            getString(R.string.status_enabled)
        } else {
            getString(R.string.status_disabled)
        }
        findViewById<Button>(R.id.btn_grant).text = if (enabled) {
            getString(R.string.btn_manage_access)
        } else {
            getString(R.string.btn_grant_access)
        }
        refreshLog()
    }

    private fun refreshLog() {
        val lines = DebugLog.getAll(this)
        findViewById<TextView>(R.id.debug_log).text =
            if (lines.isEmpty()) getString(R.string.log_empty)
            else lines.joinToString("\n\n")
    }
}
