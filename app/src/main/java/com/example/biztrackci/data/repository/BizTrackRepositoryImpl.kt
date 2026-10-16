package com.example.biztrackci.data.repository

import com.example.biztrackci.data.local.dao.ExpenseDao
import com.example.biztrackci.data.local.dao.ProductDao
import com.example.biztrackci.data.local.dao.SaleDao
import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.data.local.entity.SaleWithItems
import com.example.biztrackci.data.preferences.UserPreferencesRepository
import com.example.biztrackci.domain.model.FinancialSummary
import com.example.biztrackci.domain.repository.BizTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BizTrackRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : BizTrackRepository {

    // Products
    override fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    override fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    override fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    override suspend fun addProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    override suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    override suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    override suspend fun updateStockQuantity(productId: Long, newQuantity: Int) {
        productDao.updateStockQuantity(productId, newQuantity)
    }

    // Sales
    override suspend fun recordSale(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = saleDao.insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        saleDao.insertSaleItems(itemsWithSaleId)

        // Automatically update product stock
        itemsWithSaleId.forEach { item ->
            productDao.reduceStockQuantity(item.productId, item.quantity)
        }
        return saleId
    }

    override fun getSalesWithItems(): Flow<List<SaleWithItems>> = saleDao.getSalesWithItems()

    override fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleWithItems>> {
        return saleDao.getSalesBetweenDates(startDate, endDate)
    }

    override fun getTotalSalesBetween(startDate: Long, endDate: Long): Flow<Double> {
        return saleDao.getTotalSalesBetween(startDate, endDate).map { it ?: 0.0 }
    }

    // Expenses
    override suspend fun addExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)

    override suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    override suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    override fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    override fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Double> {
        return expenseDao.getTotalExpensesBetween(startDate, endDate).map { it ?: 0.0 }
    }

    // Financial calculations
    override fun getFinancialSummaryBetween(startDate: Long, endDate: Long): Flow<FinancialSummary> {
        return combine(
            saleDao.getSalesBetweenDates(startDate, endDate),
            expenseDao.getExpensesBetweenDates(startDate, endDate)
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

    // User Preferences
    override val userRole: Flow<String> = userPreferencesRepository.userRole
    override val businessName: Flow<String> = userPreferencesRepository.businessName
    override val subscriptionStatus: Flow<String> = userPreferencesRepository.subscriptionStatus

    override suspend fun updateRole(role: String) = userPreferencesRepository.updateRole(role)

    override suspend fun updateBusinessName(name: String) = userPreferencesRepository.updateBusinessName(name)

    override suspend fun updateSubscriptionStatus(status: String) = userPreferencesRepository.updateSubscriptionStatus(status)
}
