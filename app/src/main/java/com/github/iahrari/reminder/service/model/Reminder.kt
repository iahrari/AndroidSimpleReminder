package com.github.iahrari.reminder.service.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val type: ReminderType,
    val time: Date,
    val isEnabled: Boolean,
    val weeksDay: MutableList<Int> = ArrayList()
){
    fun getCalendar(): Calendar =
        Calendar.getInstance().apply {
            time = this@Reminder.time
        }
}