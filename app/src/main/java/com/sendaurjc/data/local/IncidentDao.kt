package com.sendaurjc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert
    suspend fun insert(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<IncidentEntity>>
}
