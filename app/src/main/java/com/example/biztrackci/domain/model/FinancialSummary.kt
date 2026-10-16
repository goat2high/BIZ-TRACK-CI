package com.example.biztrackci.domain.model

data class FinancialSummary(
    val totalRevenue: Double,
    val totalCogs: Double, // Total Cost of Goods Sold
    val totalExpenses: Double,
    val netProfit: Double
)
