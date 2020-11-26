package com.github.iahrari.reminder.ui.util

import android.content.Context
import java.util.*

object LanguageUtil {
    const val EN = "en"
    const val FA = "fa"
    fun applyLanguage(context: Context, language: String): Context{
        val locale =  Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    fun getLanguage(context: Context): String =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", EN)!!
}