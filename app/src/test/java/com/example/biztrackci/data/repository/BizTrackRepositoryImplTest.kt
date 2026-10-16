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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeProductDao : ProductDao {
    private val products = MutableStateFlow<List<ProductEntity>>(emptyList())

    override suspend fun insertProduct(product: ProductEntity): Long {
        val list = products.value.toMutableList()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        val newProduct = product.copy(id = id)
        list.add(newProduct)
        products.value = list
        return id
    }

    override suspend fun insertProducts(products: List<ProductEntity>) {
        products.forEach { insertProduct(it) }
    }

    override suspend fun updateProduct(product: ProductEntity) {
        products.value = products.value.map { if (it.id == product.id) product else it }
    }

    override suspend fun deleteProduct(product: ProductEntity) {
        products.value = products.value.filter { it.id != product.id }
    }

    override fun getAllProducts(): Flow<List<ProductEntity>> = products

    override fun getProductById(id: Long): Flow<ProductEntity?> = products.map { list -> list.find { it.id == id } }

    override fun getLowStockProducts(): Flow<List<ProductEntity>> = products.map { list -> list.filter { it.stockQuantity <= it.minStockThreshold } }

    override suspend fun updateStockQuantity(productId: Long, newQuantity: Int) {
        products.value = products.value.map { if (it.id == productId) it.copy(stockQuantity = newQuantity) else it }
    }

    override suspend fun reduceStockQuantity(productId: Long, quantity: Int) {
        products.value = products.value.map { if (it.id == productId) it.copy(stockQuantity = it.stockQuantity - quantity) else it }
    }
}

class FakeSaleDao : SaleDao {
    private val sales = MutableStateFlow<List<SaleEntity>>(emptyList())
    private val saleItems = MutableStateFlow<List<SaleItemEntity>>(emptyList())

    override suspend fun insertSale(sale: SaleEntity): Long {
        val list = sales.value.toMutableList()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        val newSale = sale.copy(id = id)
        list.add(newSale)
        sales.value = list
        return id
    }

    override suspend fun insertSaleItems(items: List<SaleItemEntity>) {
        val list = saleItems.value.toMutableList()
        var currentMax = list.maxOfOrNull { it.id } ?: 0
        items.forEach { item ->
            currentMax++
            list.add(item.copy(id = currentMax))
        }
        saleItems.value = list
    }

    override fun getSalesWithItems(): Flow<List<SaleWithItems>> {
        return sales.map { saleList ->
            saleList.map { sale ->
                SaleWithItems(sale, saleItems.value.filter { it.saleId == sale.id })
            }
        }
    }

    override fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<SaleWithItems>> {
        return sales.map { saleList ->
            saleList.filter { it.timestamp in startDate..endDate }.map { sale ->
                SaleWithItems(sale, saleItems.value.filter { it.saleId == sale.id })
            }
        }
    }

    override fun getTotalSalesBetween(startDate: Long, endDate: Long): Flow<Double?> {
        return getSalesBetweenDates(startDate, endDate).map { list ->
            list.sumOf { it.sale.totalAmount }
        }
    }
}

class FakeExpenseDao : ExpenseDao {
    private val expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())

    override suspend fun insertExpense(expense: ExpenseEntity): Long {
        val list = expenses.value.toMutableList()
        val id = (list.maxOfOrNull { it.id } ?: 0) + 1
        val newExpense = expense.copy(id = id)
        list.add(newExpense)
        expenses.value = list
        return id
    }

    override suspend fun insertExpenses(expenses: List<ExpenseEntity>) {
        expenses.forEach { insertExpense(it) }
    }

    override suspend fun updateExpense(expense: ExpenseEntity) {
        expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun deleteExpense(expense: ExpenseEntity) {
        expenses.value = expenses.value.filter { it.id != expense.id }
    }

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenses

    override fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenses.map { list -> list.filter { it.timestamp in startDate..endDate } }
    }

    override fun getTotalExpensesBetween(startDate: Long, endDate: Long): Flow<Double?> {
        return getExpensesBetweenDates(startDate, endDate).map { list -> list.sumOf { it.amount } }
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository(
    dataStore = mockDataStore()
) {
    private val _role = MutableStateFlow("OWNER")
    private val _businessName = MutableStateFlow("Boutique Abidjan Express")
    private val _subscriptionStatus = MutableStateFlow("ACTIVE")

    override val userRole: Flow<String> = _role
    override val businessName: Flow<String> = _businessName
    override val subscriptionStatus: Flow<String> = _subscriptionStatus

    override suspend fun updateRole(role: String) { _role.value = role }
    override suspend fun updateBusinessName(name: String) { _businessName.value = name }
    override suspend fun updateSubscriptionStatus(status: String) { _subscriptionStatus.value = status }

    companion object {
        private fun mockDataStore(): androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
            return object : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
                override val data: Flow<androidx.datastore.preferences.core.Preferences>
                    get() = flowOf(androidx.datastore.preferences.core.emptyPreferences())

                override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
                    return androidx.datastore.preferences.core.emptyPreferences()
                }
            }
        }
    }
}

class BizTrackRepositoryImplTest {

    @Test
    fun testRecordSaleAndFinancialSummaryCalculation() = runTest {
        val productDao = FakeProductDao()
        val saleDao = FakeSaleDao()
        val expenseDao = FakeExpenseDao()
        val userPreferencesRepository = FakeUserPreferencesRepository()

        val repository = BizTrackRepositoryImpl(
            productDao = productDao,
            saleDao = saleDao,
            expenseDao = expenseDao,
            userPreferencesRepository = userPreferencesRepository
        )

        // 1. Add product
        val productId = productDao.insertProduct(
            ProductEntity(
                name = "Riz Uncle Sam 5kg",
                category = "Alimentation",
                costPrice = 3500.0,
                sellingPrice = 4500.0,
                stockQuantity = 20,
                minStockThreshold = 5,
                unit = "Sac"
            )
        )

        // 2. Add Expense
        expenseDao.insertExpense(
            ExpenseEntity(
                title = "Transport",
                amount = 1000.0,
                category = "Transport",
                timestamp = 2000L
            )
        )

        // 3. Record Sale
        val saleId = repository.recordSale(
            sale = SaleEntity(timestamp = 2500L, totalAmount = 9000.0, paymentMethod = "Wave", staffName = "Kouassi"),
            items = listOf(
                SaleItemEntity(
                    saleId = 0,
                    productId = productId,
                    productName = "Riz Uncle Sam 5kg",
                    quantity = 2,
                    unitPrice = 4500.0,
                    costPrice = 3500.0
                )
            )
        )

        // Verify stock reduced from 20 to 18
        val updatedProduct = repository.getProductById(productId).first()
        assertEquals(18, updatedProduct?.stockQuantity)

        // Verify financial summary between timestamp 1000L and 5000L
        val summary: FinancialSummary = repository.getFinancialSummaryBetween(1000L, 5000L).first()

        assertEquals(9000.0, summary.totalRevenue, 0.01)
        assertEquals(7000.0, summary.totalCogs, 0.01) // 2 * 3500
        assertEquals(1000.0, summary.totalExpenses, 0.01)
        assertEquals(1000.0, summary.netProfit, 0.01) // 9000 - 7000 - 1000 = 1000
    }
}
