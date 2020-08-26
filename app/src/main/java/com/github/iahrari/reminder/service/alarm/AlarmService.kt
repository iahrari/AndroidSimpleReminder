package com.github.iahrari.reminder.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.MonthDisplayHelper
import com.github.iahrari.reminder.service.database.Database
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

object AlarmService {

    fun setReminder(context: Context, reminder: Reminder) {
        cancelAlarm(context, reminder)
        if (reminder.isEnabled)
            when (reminder.type) {
                ReminderType.DAILY, ReminderType.ONCE ->
                    setOnceOrDailyAlarm(context, reminder)

                ReminderType.WEEKLY, ReminderType.DAYS_OF_WEEK ->
                    setDayOfWeekAlarm(context, reminder)

                ReminderType.START_OF_MONTH -> setStartMonthAlarm(context, reminder)
                ReminderType.END_OF_MONTH -> setEndMonthAlarm(context, reminder)
                ReminderType.EXACT_TIME -> setExactTimeAlarm(context, reminder)
            }
    }

    private fun setExactTimeAlarm(context: Context, reminder: Reminder) {
        if (reminder.time.time > System.currentTimeMillis()) {
            val pIntent = getPendingIntent(context, reminder.id, 0)
            setNonRepeatingAlarm(context, reminder.time.time, pIntent)
        } else setMissedAlarm(context, reminder)
    }

    private fun setMissedAlarm(context: Context, reminder: Reminder) {
        CoroutineScope(Dispatchers.Default).launch {
            reminder.isEnabled = false
            Database.getInstance(context).getDAO().insert(reminder)
            val pIntent = getPendingIntent(context, reminder.id, 0)
            val cal = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
            }
            setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
        }
    }

    private fun setStartMonthAlarm(context: Context, reminder: Reminder) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.MONTH, 1)

        val pIntent = getPendingIntent(context, reminder.id, 0)
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private fun setEndMonthAlarm(context: Context, reminder: Reminder) {
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

        Log.i("Reminder_Month", cal.time.toString())

        val pIntent = getPendingIntent(context, reminder.id, 0)
        setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private fun setOnceOrDailyAlarm(context: Context, reminder: Reminder) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1)

        val pIntent = getPendingIntent(context, reminder.id, 0)
        Log.i("Reminder", cal.time.toString())
        if (reminder.type == ReminderType.DAILY)
            setRepeatingAlarm(context, cal.timeInMillis, 1, pIntent)
        else setNonRepeatingAlarm(context, cal.timeInMillis, pIntent)
    }

    private fun setWeeklyAlarm(context: Context, reminder: Reminder, weekDay: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.getCalendar().get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, reminder.getCalendar().get(Calendar.MINUTE))
            set(Calendar.DAY_OF_WEEK, weekDay)
            set(Calendar.SECOND, 0)
        }

        if (cal.time.time < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 7)

        val pIntent = getPendingIntent(context, reminder.id, weekDay)
        setRepeatingAlarm(context, cal.timeInMillis, 7, pIntent)
    }

    private fun setDayOfWeekAlarm(context: Context, reminder: Reminder) {
        for ((index, value) in reminder.weeksDay.withIndex()) {
            if (value == Reminder.WEEK_DAY_ENABLE)
                setWeeklyAlarm(context, reminder, index + 1)
        }
    }

    fun cancelAlarm(context: Context, vararg reminders: Reminder) {
        for (reminder in reminders) {
            val alarmManager = getAlarmManager(context)
            val requestID = reminder.id * 10

            for (i in 0..7) {
                val pIntent = getOldPendingIntent(context, requestID + i, reminder.id)
                if (pIntent != null && alarmManager != null)
                    alarmManager.cancel(pIntent)
            }
        }
    }

    private fun getAlarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun getPendingIntent(context: Context, id: Int, dayId: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            id * 10 + dayId,
            getAlarmIntent(context, id),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun getOldPendingIntent(context: Context, id: Int, reminderId: Int): PendingIntent? =
        PendingIntent.getService(
            context,
            id,
            getAlarmIntent(context, reminderId),
            PendingIntent.FLAG_NO_CREATE
        )

    private fun setRepeatingAlarm(
        context: Context,
        time: Long,
        range: Int,
        pendingIntent: PendingIntent
    ) {
        getAlarmManager(context)!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            time,
            AlarmManager.INTERVAL_DAY * range,
            pendingIntent
        )
    }

    private fun setNonRepeatingAlarm(context: Context, time: Long, pendingIntent: PendingIntent) {
        getAlarmManager(context)!!.setExact(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun calculateTillEndOfMonth(cal: Calendar): Int {
        val mHelper = MonthDisplayHelper(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        return cal.get(Calendar.DAY_OF_MONTH) - mHelper.numberOfDaysInMonth
    }

    private fun getAlarmIntent(context: Context, id: Int): Intent =
        Intent(context, AlarmBroadcastReceiver::class.java).apply {
            putExtra(AlarmBroadcastReceiver.REMINDER_ID, id)
        }
}