package com.sendaurjc

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.sendaurjc.data.local.AppDatabase
import org.osmdroid.config.Configuration

class
SendaApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "senda_db")
            .fallbackToDestructiveMigration()
            .build()
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }
}
