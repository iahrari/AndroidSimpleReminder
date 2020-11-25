package com.github.iahrari.reminder.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.iahrari.reminder.service.database.ReminderDAO
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import java.util.*

object AlarmService {

    suspend fun setReminder(context: Context, reminder: Reminder, dao: ReminderDAO) {
        cancelAlarm(context, reminder)
        if (reminder.isEnabled)
            when (reminder.type) {
                ReminderType.DAILY, ReminderType.ONCE ->
                    setOnceOrDailyAlarm(context, reminder, dao)

                ReminderType.WEEKLY, ReminderType.DAYS_OF_WEEK ->
                    setDayOfWeekAlarm(context, reminder, dao)

                ReminderType.START_OF_MONTH -> setStartMonthAlarm(context, reminder, dao)
                ReminderType.END_OF_MONTH -> setEndMonthAlarm(context, reminder, dao)
                ReminderType.EXACT_TIME -> setExactTimeAlarm(context, reminder, dao)
            }
    }

    private suspend fun setExactTimeAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        if (reminder.time.time > System.currentTimeMillis()) {
            val pIntent = getPendingIntent(context, reminder.id * 10)
            setNonRepeatingAlarm(context, reminder.time.time, pIntent)
        } else setMissedAlarm(context, reminder, dao)
    }

    private suspend fun setMissedAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        reminder.isEnabled = false
        dao.insert(reminder)
        val pIntent = getPendingIntent(context, reminder.id * 10)
        val cal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
        }
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)

    }

    private suspend fun setStartMonthAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.MONTH, 1)

        reminder.time = cal.time
        dao.insert(reminder)

        val pIntent = getPendingIntent(context, reminder.id * 10)
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private suspend fun setEndMonthAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        reminder.time = cal.time
        dao.insert(reminder)

        val pIntent = getPendingIntent(context, reminder.id * 10)
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private suspend fun setOnceOrDailyAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1)

        reminder.time = cal.time
        dao.insert(reminder)

        val pIntent = getPendingIntent(context, reminder.id * 10)
        Log.i("Reminder", cal.time.toString())
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    suspend fun setWeeklyAlarm(context: Context, reminder: Reminder, weekDay: Int, dao: ReminderDAO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.DAY_OF_WEEK, weekDay)
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 7)

        if (reminder.type == ReminderType.WEEKLY) {
            reminder.time = cal.time
            dao.insert(reminder)
        }
        val pIntent = getPendingIntent(context, reminder.id * 10 + weekDay)
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private suspend fun setDayOfWeekAlarm(context: Context, reminder: Reminder, dao: ReminderDAO) {
        for ((index, value) in reminder.weeksDay.withIndex()) {
            if (value == Reminder.WEEK_DAY_ENABLE)
                setWeeklyAlarm(context, reminder, index + 1, dao)
        }
    }

    fun cancelAlarm(context: Context, vararg reminders: Reminder) {
        val alarmManager = getAlarmManager(context)
        for (reminder in reminders) {
            val requestID = reminder.id

            for (i in 0..7) {
                val pIntent = getPendingIntent(context, requestID * 10 + i)
                pIntent.cancel()
                alarmManager?.cancel(pIntent)
            }
        }
    }

    private fun getAlarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun getPendingIntent(context: Context, id: Int): PendingIntent =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                id,
                getAlarmIntent(context, id),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                context,
                id,
                getAlarmIntent(context, id),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    private fun setNonRepeatingAlarm(context: Context, time: Long, pendingIntent: PendingIntent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getAlarmManager(context)!!.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        } else getAlarmManager(context)!!.setExact(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun getAlarmIntent(context: Context, id: Int): Intent =
        Intent(context, ReminderService::class.java).apply {
            putExtra(ReminderService.REMINDER_ID, id)
            action = ReminderService.INTENT_ACTION
        }
}