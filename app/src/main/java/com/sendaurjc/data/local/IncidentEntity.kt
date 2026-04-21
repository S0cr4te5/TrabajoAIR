package com.sendaurjc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val lat: Double,
    val lon: Double,
    val timestamp: Long = System.currentTimeMillis()
)
