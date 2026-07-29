package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cli_history")
data class CliHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val output: String,
    val isSuccess: Boolean,
    val timestamp: String,
    val executionTimeMs: Long
)
