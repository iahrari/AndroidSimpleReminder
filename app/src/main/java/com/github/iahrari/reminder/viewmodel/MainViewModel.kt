package com.github.iahrari.reminder.viewmodel

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.github.iahrari.reminder.service.alarm.AlarmService
import com.github.iahrari.reminder.service.database.ReminderDAO
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainViewModel @ViewModelInject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ReminderDAO
) : ViewModel() {
    private var reminderOriginal: Reminder? = null

    fun getReminders(): LiveData<List<Reminder>> =
        dao.getAllReminders()

    fun getReminder(id: Int): LiveData<Reminder> {
        val liveData = MutableLiveData<Reminder>()
        if (id >= 0) {
            viewModelScope.launch(Dispatchers.Default) {
                reminderOriginal = dao.getReminderById(id)
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

    fun deleteReminders(vararg reminders: Reminder) {
        viewModelScope.launch(Dispatchers.Default) {
            AlarmService.cancelAlarm(context, *reminders)
            dao.deleteReminders(*reminders)
        }
    }

    fun insertOrUpdate(reminder: Reminder, isEnableChanged: Boolean) {
        viewModelScope.launch {
            if (isReminderUpdated(reminder) || isEnableChanged) {
                if (!isEnableChanged) reminder.isEnabled = true
            }

            reminder.id = withContext(Dispatchers.Default){
                dao.insert(reminder)
            }.toInt()

            update(reminder, isEnableChanged)
        }
    }

    private fun update(reminder: Reminder, isEnableChanged: Boolean){
        if (!isEnableChanged)
            reminderOriginal = reminder
        AlarmService.setReminder(context, reminder)

    }

    fun isReminderUpdated(reminderClone: Reminder?): Boolean =
         reminderClone != null && reminderClone != reminderOriginal

    private fun getEmptyReminder(): Reminder {
        return Reminder("", ReminderType.DAILY, Date(), true).apply {
            getCalendar().set(Calendar.SECOND, 0)
        }
    }
}