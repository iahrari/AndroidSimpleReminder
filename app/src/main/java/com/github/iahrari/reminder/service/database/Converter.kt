package com.github.iahrari.reminder.service.database

import androidx.room.TypeConverter
import com.github.iahrari.reminder.service.model.ReminderType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.StringBuilder

class Converter {
    @TypeConverter
    fun fromTimeStamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun fromStringType(value: String): ReminderType {
        return ReminderType.valueOf(value)
    }

    @TypeConverter
    fun reminderTypeToString(value: ReminderType): String {
        return value.name
    }

    @TypeConverter
    fun listToString(value: List<Int>): String {
        val s = StringBuilder("")
        if(value.isNotEmpty()){
            for(i in value){
                s.append("$i,")
            }
        }

        return s.toString()
    }

    @TypeConverter
    fun stringToList(value: String?): MutableList<Int> {
        val list = ArrayList<Int>()
        if (value != null && value.contains(',')){
            val s = value.split(',')
            for(data in s){
                if(data.isNotEmpty())
                    list.add(data.toInt())
            }
        }

        return list
    }
}