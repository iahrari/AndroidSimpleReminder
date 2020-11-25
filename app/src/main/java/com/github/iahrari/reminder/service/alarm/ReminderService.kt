package com.github.iahrari.reminder.service.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.service.database.ReminderDAO
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import com.github.iahrari.reminder.ui.adapter.BindingAdapter
import com.github.iahrari.reminder.ui.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : Service() {
    @Inject lateinit var dao: ReminderDAO

    private fun setForegroundNotification() {
        createNotificationChannel(applicationContext)
        serviceNotification(applicationContext)
        val pIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            Intent(applicationContext, MainActivity::class.java),
            0
        )
        val notification = NotificationCompat.Builder(applicationContext, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentIntent(pIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        startForeground(-100, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setForegroundNotification()
        val alarmId = intent!!.getIntExtra(REMINDER_ID, -1)
        val id = alarmId / 10
        if (alarmId != -1 && intent.action == INTENT_ACTION) {
            val i = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(REMINDER_ID, id)
                action = "REMINDER_ID$alarmId"
                this.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, i, 0)

            CoroutineScope(Dispatchers.Main).launch {
                val dayOfWeek = alarmId % 10

                val reminder: Reminder =
                    withContext(Dispatchers.Default) {
                        dao.getReminderById(id)
                    }

                val title =
                    if (!reminder.isEnabled) applicationContext.getString(R.string.you_missed)
                    else applicationContext.getString(R.string.do_not_forget)

                val text = if (reminder.title == "")
                    BindingAdapter.makeTitle(reminder, applicationContext.resources)
                else reminder.title
                val notificationBuilder = NotificationCompat.Builder(
                    applicationContext,
                    CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)


                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(id, notificationBuilder.build())
                }
                //startForeground(reminder.id, notificationBuilder.build())

                if (reminder.type == ReminderType.EXACT_TIME || reminder.type == ReminderType.ONCE) {
                    reminder.isEnabled = false
                    withContext(Dispatchers.Default) {
                        dao.insert(reminder)
                    }
                } else if (
                    reminder.type == ReminderType.END_OF_MONTH ||
                    reminder.type == ReminderType.START_OF_MONTH ||
                    reminder.type == ReminderType.DAILY ||
                    reminder.type == ReminderType.WEEKLY
                )
                    AlarmService.setReminder(applicationContext, reminder, dao)
                else AlarmService.setWeeklyAlarm(applicationContext, reminder, dayOfWeek, dao)

                stopForeground(false)
                stopSelf()
            }
        } else {
            val pIntent = PendingIntent.getActivity(
                applicationContext,
                1,
                Intent(applicationContext, MainActivity::class.java),
                0
            )

            val notification = NotificationCompat.Builder(applicationContext, SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setSilent(true)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)

            startForeground(100, notification.build())
            CoroutineScope(Dispatchers.Default).launch {
                val list = dao.getAllRemindersList()
                for (r in list) {
                    if (r.isEnabled)
                        AlarmService.setReminder(applicationContext, r, dao)
                }
                stopForeground(true)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
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

    private fun serviceNotification(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = context.getString(R.string.app_name)
        val descriptionText = "Channel of reminder app service"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(SERVICE_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        const val REMINDER_ID = "reminder_id"
        const val CHANNEL_ID = "reminder notification"
        const val SERVICE_CHANNEL_ID = "reminder notification service"
        const val INTENT_ACTION = "com.github.io.iahrari.reminder.service.alarm.ReminderService"
    }
}