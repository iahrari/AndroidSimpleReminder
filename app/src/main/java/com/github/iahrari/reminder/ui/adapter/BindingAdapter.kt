package com.github.iahrari.reminder.ui.adapter

import android.content.res.Resources
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import java.lang.StringBuilder
import java.util.*

object BindingAdapter {
    @BindingAdapter("app:setDateText")
    @JvmStatic
    fun setDateText(view: TextView, reminder: Reminder) {
        val res = view.context.resources
        val cal = reminder.getCalendar()
        val type = reminder.type

        val string = if (type == ReminderType.EXACT_TIME || type == ReminderType.ONCE)
            getDateText(cal, res)
        else if(type == ReminderType.WEEKLY)
            "${res.getStringArray(R.array.weekDays)[reminder.weeksDay.indexOf(Reminder.WEEK_DAY_ENABLE)]}${res.getString(R.string.plural)}"
        else if(type == ReminderType.DAYS_OF_WEEK)
            getReminderWeekDays(reminder.weeksDay, res)
        else res.getString(
            when (type) {
                ReminderType.END_OF_MONTH -> R.string.end_of_month
                ReminderType.START_OF_MONTH -> R.string.start_of_month
                else -> R.string.daily
            }
        )

        view.text = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun getDateText(cal: Calendar, res: Resources): String{
        val comma = res.getString(R.string.comma)
        return """${cal.get(Calendar.YEAR)}$comma
                | ${res.getStringArray(R.array.months)[cal.get(Calendar.MONTH)]}$comma
                | ${cal.get(Calendar.DAY_OF_MONTH)} """.trimMargin()
    }

    private fun getReminderWeekDays(array: IntArray, res: Resources): String{
        val weekArray = res.getStringArray(R.array.weekDaySmall)
        val s = StringBuilder("")

        s.append(getColoredWeekDay(6, array[6], weekArray))

        for ((index, isEnabled) in array.withIndex())
            if (index != 6)
                s.append(getColoredWeekDay(index, isEnabled, weekArray))


        return s.toString()
    }

    private fun getColoredWeekDay(index: Int, isEnabled: Int, weekArray: Array<String>): String =
        if (isEnabled == Reminder.WEEK_DAY_ENABLE)
            "<font color=#E91E63>${weekArray[index]}</font>"
        else
            "<font color=#FCE4EC>${weekArray[index]}</font>"
}