package com.github.iahrari.reminder.service.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.iahrari.reminder.service.model.Reminder

@Dao
interface ReminderDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: Reminder)

    @Query("select * from reminders")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("select * from reminders where id=:id")
    fun getReminderById(id: Int): LiveData<Reminder>

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}