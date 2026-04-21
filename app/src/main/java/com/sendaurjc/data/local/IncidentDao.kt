package com.sendaurjc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert
    suspend fun insert(incident: IncidentEntity)

    @Update
    suspend fun update(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<IncidentEntity>>

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE incidents SET type = :type WHERE id = :id")
    suspend fun updateType(id: Long, type: String)
}
