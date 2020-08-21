package com.github.iahrari.reminder

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val theme = applicationContext
            .getSharedPreferences("theme", Context.MODE_PRIVATE)
            .getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        AppCompatDelegate.setDefaultNightMode(theme)
    }
}