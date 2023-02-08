package com.example.bmta

import android.app.Application
import com.example.bmta.di.databaseModule
import com.example.bmta.di.repoModule
import com.example.bmta.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(listOf(databaseModule, repoModule, viewModelModule))
        }
    }
}