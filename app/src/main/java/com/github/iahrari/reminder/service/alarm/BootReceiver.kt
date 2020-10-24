package com.github.iahrari.reminder.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.github.iahrari.reminder.service.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Toast.makeText(context, "Setting reminders", Toast.LENGTH_LONG).show()
        val i = Intent(context, ReminderService::class.java).apply {
            putExtra(ReminderService.REMINDER_ID, -1)
            action = Intent.ACTION_BOOT_COMPLETED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i)
        } else context.startService(i)

//        if (intent != null && Intent.ACTION_BOOT_COMPLETED == intent.action)
//            CoroutineScope(Dispatchers.Default).launch {
//                val list = Database.getInstance(context).getDAO().getAllRemindersList()
//                for (r in list){
//                    if (r.isEnabled)
//                        AlarmService.setReminder(context, r)
//                }
//            }
    }
}