package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CliHistoryDao {

    @Query("SELECT * FROM cli_history ORDER BY id DESC LIMIT 50")
    fun getAllHistory(): Flow<List<CliHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CliHistoryEntity)

    @Query("DELETE FROM cli_history")
    suspend fun clearHistory()
}
