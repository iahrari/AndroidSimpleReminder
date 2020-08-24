package com.github.iahrari.reminder.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.github.iahrari.reminder.service.database.Database
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel @ViewModelInject constructor(
    private val database: Database
) : ViewModel() {
    private var reminderOriginal: Reminder? = null

    fun getReminders(): LiveData<List<Reminder>> =
        database.getDAO().getAllReminders()

    fun getReminder(id: Int): LiveData<Reminder> {
        val liveData = MutableLiveData<Reminder>()
        if (id >= 0) {
            viewModelScope.launch(Dispatchers.Default) {
                reminderOriginal = database.getDAO().getReminderById(id)
                liveData.postValue(reminderOriginal!!.copy(weeksDay = reminderOriginal!!.weeksDay.copyOf()).apply {
                    this.id = reminderOriginal!!.id
                })
            }
        }
        else {
            if (reminderOriginal == null){
                reminderOriginal = getEmptyReminder()
                liveData.postValue(reminderOriginal!!.copy())
            }
        }

        return liveData
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.Default) {
            database.getDAO().deleteReminder(reminder)
        }
    }

    fun deleteReminders(vararg reminders: Reminder) {
        viewModelScope.launch(Dispatchers.Default) {
            database.getDAO().deleteReminders(*reminders)
        }
    }

    fun insertOrUpdate(reminder: Reminder, isEnableChanged: Boolean) {
        if (isReminderUpdated(reminder) || isEnableChanged) {
            if (!isEnableChanged) reminder.isEnabled = true
            update(reminder, isEnableChanged)
        }

        viewModelScope.launch(Dispatchers.Default) {
            database.getDAO().insert(reminder)
        }
    }

    private fun update(reminder: Reminder, isEnableChanged: Boolean){
        if (!isEnableChanged)
            reminderOriginal = reminder

    }

    fun isReminderUpdated(reminderClone: Reminder?): Boolean =
         reminderClone != null && reminderClone != reminderOriginal

    private fun getEmptyReminder(): Reminder {
        return Reminder("", ReminderType.DAILY, Date(), true)
    }
}