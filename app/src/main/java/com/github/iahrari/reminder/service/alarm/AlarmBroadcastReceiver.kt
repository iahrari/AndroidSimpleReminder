package com.github.iahrari.reminder.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.github.iahrari.reminder.service.alarm.ReminderService.Companion.REMINDER_ID

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        val id = intent.getIntExtra(REMINDER_ID, -1)
//        if (id == -1) return
//
//        Log.i("ReminderEditBroadcast", id.toString())
//
//        val i = Intent(context.applicationContext, ReminderService::class.java).apply {
//            putExtra(REMINDER_ID, id)
//            action = INTENT_ACTION
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(i)
//        } else context.startService(i)
    }

//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
//
//        val name = context.getString(R.string.app_name) + context.getString(R.string.plural)
//        val descriptionText = "Channel of reminder app"
//        val importance = NotificationManager.IMPORTANCE_HIGH
//        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//            description = descriptionText
//        }
//
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel)
//
//    }

    companion object {


    }
}