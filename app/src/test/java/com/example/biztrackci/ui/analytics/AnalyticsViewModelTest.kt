package com.example.biztrackci.ui.analytics

import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.data.repository.FakeBizTrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBizTrackRepository
    private lateinit var viewModel: AnalyticsViewModel

    private val now = System.currentTimeMillis()

    private val product = ProductEntity(
        id = 1,
        name = "Sac de Riz Uncle Sam 5kg",
        category = "Alimentation",
        costPrice = 3000.0,
        sellingPrice = 4500.0,
        stockQuantity = 2, // low stock!
        minStockThreshold = 5,
        unit = "Sac"
    )

    private val saleEntity1 = SaleEntity(
        id = 1,
        timestamp = now - 1000,
        totalAmount = 9000.0,
        paymentMethod = "Wave",
        staffName = "Propriétaire"
    )
    private val saleItem1 = SaleItemEntity(
        id = 1,
        saleId = 1,
        productId = 1,
        productName = "Sac de Riz Uncle Sam 5kg",
        quantity = 2,
        unitPrice = 4500.0,
        costPrice = 3000.0
    )

    private val saleEntity2 = SaleEntity(
        id = 2,
        timestamp = now - 500,
        totalAmount = 4500.0,
        paymentMethod = "Orange Money",
        staffName = "Propriétaire"
    )
    private val saleItem2 = SaleItemEntity(
        id = 2,
        saleId = 2,
        productId = 1,
        productName = "Sac de Riz Uncle Sam 5kg",
        quantity = 1,
        unitPrice = 4500.0,
        costPrice = 3000.0
    )

    private val expenseEntity = ExpenseEntity(
        id = 1,
        title = "Transport livraison",
        amount = 1500.0,
        category = "Transport",
        timestamp = now - 800
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBizTrackRepository()

        repository.productsFlow.value = listOf(product)
        repository.recordSale(saleEntity1, listOf(saleItem1))
        repository.recordSale(saleEntity2, listOf(saleItem2))
        repository.addExpense(expenseEntity)

        viewModel = AnalyticsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun timeRangeSelection_updatesSelectedTimeRangeInState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setTimeRange(TimeRange.ALL_TIME)

        val state = viewModel.uiState.value
        assertEquals(TimeRange.ALL_TIME, state.selectedTimeRange)
    }

    @Test
    fun financialSummary_calculatesRevenueCogsExpensesAndNetProfit() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setTimeRange(TimeRange.ALL_TIME)

        val state = viewModel.uiState.value
        val summary = state.financialSummary

        // Total Revenue = 9000 + 4500 = 13500.0
        assertEquals(13500.0, summary.totalRevenue, 0.01)

        // Total COGS = (2 * 3000) + (1 * 3000) = 9000.0
        assertEquals(9000.0, summary.totalCogs, 0.01)

        // Total Expenses = 1500.0
        assertEquals(1500.0, summary.totalExpenses, 0.01)

        // Net Profit = 13500 - 9000 - 1500 = 3000.0
        assertEquals(3000.0, summary.netProfit, 0.01)
    }

    @Test
    fun paymentMethodsBreakdown_computesCorrectAmountsAndPercentages() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setTimeRange(TimeRange.ALL_TIME)

        val stats = viewModel.uiState.value.paymentMethodStats
        val waveStat = stats.find { it.methodName == "Wave" }
        val orangeStat = stats.find { it.methodName == "Orange Money" }

        assertEquals(9000.0, waveStat?.amount ?: 0.0, 0.01)
        assertEquals(4500.0, orangeStat?.amount ?: 0.0, 0.01)

        // Total revenue = 13500. Wave % = 9000/13500 = 66.67%, Orange % = 4500/13500 = 33.33%
        assertEquals(66.67f, waveStat?.percentage ?: 0f, 1.0f)
        assertEquals(33.33f, orangeStat?.percentage ?: 0f, 1.0f)
    }

    @Test
    fun lowStockAlert_identifiesLowStockProductsCount() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertEquals(1, state.lowStockProductsCount)
        assertEquals(1, state.lowStockProducts.size)
        assertEquals("Sac de Riz Uncle Sam 5kg", state.lowStockProducts[0].name)
    }
}
