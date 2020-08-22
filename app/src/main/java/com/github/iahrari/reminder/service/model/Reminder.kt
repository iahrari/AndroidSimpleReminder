package com.github.iahrari.reminder.service.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var type: ReminderType,
    var time: Date,
    var isEnabled: Boolean,
    var weeksDay: MutableList<Int> = ArrayList()
){
    @Ignore var isSelected = false
    fun getCalendar(): Calendar =
        Calendar.getInstance().apply {
            time = this@Reminder.time
        }
}