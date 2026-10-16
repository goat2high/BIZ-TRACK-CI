package com.example.biztrackci.domain.repository

import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.data.local.entity.SaleWithItems
import com.example.biztrackci.domain.model.FinancialSummary
import kotlinx.coroutines.flow.Flow

interface BizTrackRepository {
    // Products
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    fun getProductById(id: Long): Flow<ProductEntity?>
    suspend fun addProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun updateStockQuantity(productId: Long, newQuantity: Int)

    // Sales
    suspend fun recordSale(sale: SaleEntity, items: List<SaleItemEntity>): Long
    fun getSalesWithItems(): Flow<List<SaleWithItems>>
    fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleWithItems>>
    fun getTotalSalesBetween(startDate: Long, endDate: Long): Flow<Double>

    // Expenses
    suspend fun addExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Double>

    // Business Logic / Financial Calculations
    fun getFinancialSummaryBetween(startDate: Long, endDate: Long): Flow<FinancialSummary>

    // Preferences
    val userRole: Flow<String>
    val businessName: Flow<String>
    val subscriptionStatus: Flow<String>
    suspend fun updateRole(role: String)
    suspend fun updateBusinessName(name: String)
    suspend fun updateSubscriptionStatus(status: String)
}
