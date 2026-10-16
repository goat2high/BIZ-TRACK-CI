package com.example.biztrackci.data.repository

import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.data.local.entity.SaleWithItems
import com.example.biztrackci.domain.model.FinancialSummary
import com.example.biztrackci.domain.repository.BizTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeBizTrackRepository : BizTrackRepository {

    val productsFlow = MutableStateFlow<List<ProductEntity>>(emptyList())
    val salesFlow = MutableStateFlow<List<SaleWithItems>>(emptyList())
    val expensesFlow = MutableStateFlow<List<ExpenseEntity>>(emptyList())

    val userRoleFlow = MutableStateFlow("OWNER")
    val businessNameFlow = MutableStateFlow("BIZ TRACK CI")
    val subscriptionStatusFlow = MutableStateFlow("ACTIVE")

    override fun getAllProducts(): Flow<List<ProductEntity>> = productsFlow

    override fun getLowStockProducts(): Flow<List<ProductEntity>> =
        productsFlow.map { list -> list.filter { it.stockQuantity <= it.minStockThreshold } }

    override fun getProductById(id: Long): Flow<ProductEntity?> =
        productsFlow.map { list -> list.find { it.id == id } }

    override suspend fun addProduct(product: ProductEntity): Long {
        val current = productsFlow.value.toMutableList()
        val id = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newProduct = product.copy(id = id)
        current.add(newProduct)
        productsFlow.value = current
        return id
    }

    override suspend fun updateProduct(product: ProductEntity) {
        productsFlow.value = productsFlow.value.map { if (it.id == product.id) product else it }
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        productsFlow.value = productsFlow.value.filter { it.id != product.id }
    }

    override suspend fun updateStockQuantity(productId: Long, newQuantity: Int) {
        productsFlow.value = productsFlow.value.map {
            if (it.id == productId) it.copy(stockQuantity = newQuantity) else it
        }
    }

    override suspend fun recordSale(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val currentSales = salesFlow.value.toMutableList()
        val saleId = (currentSales.maxOfOrNull { it.sale.id } ?: 0) + 1
        val newSale = sale.copy(id = saleId)
        val itemsWithId = items.mapIndexed { index, item -> item.copy(id = (index + 1).toLong(), saleId = saleId) }

        currentSales.add(SaleWithItems(newSale, itemsWithId))
        salesFlow.value = currentSales

        // Reduce stock
        items.forEach { item ->
            productsFlow.value = productsFlow.value.map {
                if (it.id == item.productId) it.copy(stockQuantity = it.stockQuantity - item.quantity) else it
            }
        }
        return saleId
    }

    override fun getSalesWithItems(): Flow<List<SaleWithItems>> = salesFlow

    override fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleWithItems>> =
        salesFlow.map { list -> list.filter { it.sale.timestamp in startDate..endDate } }

    override fun getTotalSalesBetween(startDate: Long, endDate: Long): Flow<Double> =
        getSalesBetweenDates(startDate, endDate).map { list -> list.sumOf { it.sale.totalAmount } }

    override suspend fun addExpense(expense: ExpenseEntity): Long {
        val current = expensesFlow.value.toMutableList()
        val id = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newExpense = expense.copy(id = id)
        current.add(newExpense)
        expensesFlow.value = current
        return id
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        expensesFlow.value = expensesFlow.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expensesFlow.value = expensesFlow.value.filter { it.id != expense.id }
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expensesFlow

    override fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expensesFlow.map { list -> list.filter { it.timestamp in startDate..endDate } }

    override fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Double> =
        getExpensesBetweenDates(startDate, endDate).map { list -> list.sumOf { it.amount } }

    override fun getFinancialSummaryBetween(startDate: Long, endDate: Long): Flow<FinancialSummary> {
        return combine(
            getSalesBetweenDates(startDate, endDate),
            getExpensesBetweenDates(startDate, endDate)
        ) { salesWithItems, expenses ->
            val totalRevenue = salesWithItems.sumOf { it.sale.totalAmount }
            val totalCogs = salesWithItems.sumOf { saleWithItems ->
                saleWithItems.items.sumOf { item -> item.costPrice * item.quantity }
            }
            val totalExpenses = expenses.sumOf { it.amount }
            val netProfit = totalRevenue - totalCogs - totalExpenses

            FinancialSummary(
                totalRevenue = totalRevenue,
                totalCogs = totalCogs,
                totalExpenses = totalExpenses,
                netProfit = netProfit
            )
        }
    }

    override val userRole: Flow<String> = userRoleFlow
    override val businessName: Flow<String> = businessNameFlow
    override val subscriptionStatus: Flow<String> = subscriptionStatusFlow

    override suspend fun updateRole(role: String) {
        userRoleFlow.value = role
    }

    override suspend fun updateBusinessName(name: String) {
        businessNameFlow.value = name
    }

    override suspend fun updateSubscriptionStatus(status: String) {
        subscriptionStatusFlow.value = status
    }
}
