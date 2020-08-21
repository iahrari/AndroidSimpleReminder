package com.github.iahrari.reminder.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.iahrari.reminder.service.database.Database
import com.github.iahrari.reminder.service.model.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val database: Database
): ViewModel() {

    fun getReminders(): LiveData<List<Reminder>> =
         database.getDAO().getAllReminders()

    fun deleteReminder(reminder: Reminder){
        viewModelScope.launch(Dispatchers.Default){
            database.getDAO().deleteReminder(reminder)
        }
    }

    fun insertOrUpdate(reminder: Reminder){
        viewModelScope.launch(Dispatchers.Default){
            database.getDAO().insert(reminder)
        }
    }
}