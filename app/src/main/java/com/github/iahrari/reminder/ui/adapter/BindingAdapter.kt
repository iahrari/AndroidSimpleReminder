package com.github.iahrari.reminder.ui.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType

object BindingAdapter {
    @BindingAdapter("app:setDateText")
    @JvmStatic fun setDateText(view: TextView, reminder: Reminder){
//        if (reminder.type == )

        val string = when(reminder.type){
            ReminderType.END_OF_MONTH -> R.string.end_of_month
            ReminderType.START_OF_MONTH -> R.string.start_of_month
            else -> 0
        }


        view.setText(string)
    }
}