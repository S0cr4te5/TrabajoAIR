package com.sendaurjc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IncidentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
}
