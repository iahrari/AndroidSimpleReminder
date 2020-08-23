package com.github.iahrari.reminder.service.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "reminders")
data class Reminder(
    var title: String,
    var type: ReminderType,
    var time: Date,
    var isEnabled: Boolean,
    var weeksDay: IntArray = intArrayOf(0, 0, 0, 0, 0, 0, 0)
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @Ignore var isSelected = false
    fun getCalendar(): Calendar =
        Calendar.getInstance().apply {
            time = this@Reminder.time
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reminder

        if (title != other.title) return false
        if (type != other.type) return false
        if (time != other.time) return false
        if (isEnabled != other.isEnabled) return false
        if (!weeksDay.contentEquals(other.weeksDay)) return false
        if (id != other.id) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + isEnabled.hashCode()
        result = 31 * result + weeksDay.contentHashCode()
        result = 31 * result + id
        result = 31 * result + isSelected.hashCode()
        return result
    }

    companion object{
        const val WEEK_DAY_ENABLE = 1
        const val WEEK_DAY_DISABLE = 0
    }
}