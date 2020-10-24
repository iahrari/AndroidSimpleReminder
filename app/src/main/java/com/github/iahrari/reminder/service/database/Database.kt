package com.github.iahrari.reminder.service.database

import android.content.Context
import androidx.room.Room
import androidx.room.Database as DatabaseA
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.iahrari.reminder.service.model.Reminder

@DatabaseA(entities = [Reminder::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class Database: RoomDatabase() {

    abstract fun getDAO(): ReminderDAO

    companion object{
        private var INSTANCE: Database? = null
        fun getInstance(context: Context): Database{
            if(INSTANCE == null)
                INSTANCE = Room.databaseBuilder(context, Database::class.java, "DB").build()

            return INSTANCE!!
        }
    }
}