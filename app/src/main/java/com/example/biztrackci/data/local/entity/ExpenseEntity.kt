package com.example.biztrackci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Rent", "Utilities", "Wages", "Supplier", "Transport", "Other"
    val timestamp: Long,
    val receiptImagePath: String? = null,
    val notes: String? = null
)
