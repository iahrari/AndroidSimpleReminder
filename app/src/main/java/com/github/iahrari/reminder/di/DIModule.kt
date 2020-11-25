package com.github.iahrari.reminder.di

import android.content.Context
import com.github.iahrari.reminder.service.database.Database
import com.github.iahrari.reminder.service.database.ReminderDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
object DIModule {

    @Singleton
    @Provides
    fun provideDatabaseDAO(@ApplicationContext context: Context): ReminderDAO {
        return Database.getInstance(context).getDAO()
    }
}