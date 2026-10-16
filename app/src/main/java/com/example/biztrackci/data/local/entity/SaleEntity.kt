package com.example.biztrackci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val totalAmount: Double,
    val paymentMethod: String, // "Cash", "Wave", "Orange Money", "MTN Mobile Money"
    val staffName: String
)
