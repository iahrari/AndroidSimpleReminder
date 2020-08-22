package com.github.iahrari.reminder.di

import android.content.Context
import com.github.iahrari.reminder.service.database.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext


@InstallIn(ApplicationComponent::class)
@Module
object DIModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Database.getInstance(context)
    }
}