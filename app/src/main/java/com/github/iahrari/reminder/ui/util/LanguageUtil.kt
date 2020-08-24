package com.github.iahrari.reminder.ui.util

import android.content.Context
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.util.*

object LanguageUtil {
    const val EN = "en"
    const val FA = "fa"
    const val DEFAULT = "default"
    fun applyLanguage(context: Context, language: String): Context{
        val locale =  Locale(
            if (language == DEFAULT){
                ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].toLanguageTag()
            } else language
        )
        val configuration = context.resources.configuration

        Locale.setDefault(locale)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}