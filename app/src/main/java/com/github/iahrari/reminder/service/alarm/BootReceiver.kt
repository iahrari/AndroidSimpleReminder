package com.github.iahrari.reminder.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.iahrari.reminder.service.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED == intent.action)
            CoroutineScope(Dispatchers.Default).launch {
                val list = Database.getInstance(context).getDAO().getAllRemindersList()
                for (r in list){
                    if (r.isEnabled)
                        AlarmService.setReminder(context, r)
                }
            }
    }
}