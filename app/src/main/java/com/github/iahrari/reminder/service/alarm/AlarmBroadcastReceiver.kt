package com.github.iahrari.reminder.service.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.service.database.Database
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import com.github.iahrari.reminder.ui.adapter.BindingAdapter
import com.github.iahrari.reminder.ui.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent!!.getIntExtra(REMINDER_ID, -1)
        if (id == -1) return

        val i = Intent(context!!.applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(REMINDER_ID, id)
            action = REMINDER_ID
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)

        createNotificationChannel(context)
        val dao = Database.getInstance(context).getDAO()
        val resultPending = goAsync()
        Log.i("idReminder", id.toString())
        CoroutineScope(Dispatchers.Main).launch{
            val reminder: Reminder =
                withContext(Dispatchers.Default) {
                    dao.getReminderById(id)
                }
            if (reminder.type == ReminderType.END_OF_MONTH || reminder.type == ReminderType.START_OF_MONTH)
                AlarmService.setReminder(context, reminder)

            val title = if (!reminder.isEnabled) context.getString(R.string.you_missed)
                else context.getString(R.string.dont_forget)

            val text = if (reminder.title == "")
                BindingAdapter.getDateText(reminder.getCalendar(), context.resources)
            else reminder.title
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (reminder.type == ReminderType.EXACT_TIME || reminder.type == ReminderType.ONCE){
                reminder.isEnabled = false
                withContext(Dispatchers.Default){
                    dao.insert(reminder)
                }
            }

            with(NotificationManagerCompat.from(context)) {
                notify(id, notificationBuilder.build())
                resultPending.finish()
            }

        }

    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = context.getString(R.string.app_name) + context.getString(R.string.plural)
        val descriptionText = "Channel of reminder app"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    companion object {
        const val REMINDER_ID = "id"
        const val CHANNEL_ID = "reminder notification"
    }
}