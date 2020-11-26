package com.github.iahrari.reminder

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.github.iahrari.reminder.ui.util.LanguageUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val theme = applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        AppCompatDelegate.setDefaultNightMode(theme)
    }
}