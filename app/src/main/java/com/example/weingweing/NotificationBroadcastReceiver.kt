package com.example.weingweing
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        i.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        i.putExtra(Settings.EXTRA_CHANNEL_ID, Foreground.CHANNEL_ID)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

}