package com.example.biztrackci.ui.expenses

import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.data.repository.FakeBizTrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBizTrackRepository
    private lateinit var viewModel: ExpensesViewModel

    private val expenseRent = ExpenseEntity(
        id = 1,
        title = "Loyer boutique",
        amount = 100000.0,
        category = "Loyer",
        timestamp = System.currentTimeMillis() - 100000,
        notes = "Loyer du mois de Mai"
    )

    private val expenseUtilities = ExpenseEntity(
        id = 2,
        title = "Facture CIE Electricité",
        amount = 25000.0,
        category = "Services",
        timestamp = System.currentTimeMillis() - 50000,
        notes = "Electricité"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBizTrackRepository()
        repository.expensesFlow.value = listOf(expenseRent, expenseUtilities)
        viewModel = ExpensesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filter_expensesByCategory_returnsOnlyMatchingExpenses() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setSelectedCategory("Loyer")

        val state = viewModel.uiState.value
        assertEquals(2, state.expenses.size)
        assertEquals(1, state.filteredExpenses.size)
        assertEquals("Loyer boutique", state.filteredExpenses[0].title)
        assertEquals(100000.0, state.totalExpensesAmount, 0.01)
    }

    @Test
    fun addExpense_validInput_insertsExpenseIntoRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.addExpense(
            title = "Achat Carburant Livraison",
            amount = 15000.0,
            category = "Transport",
            notes = "Livraison Abobo",
            receiptImagePath = null
        )

        val allExpenses = repository.getAllExpenses().first()
        assertEquals(3, allExpenses.size)
        assertTrue(allExpenses.any { it.title == "Achat Carburant Livraison" })
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun deleteExpense_removesExpenseFromRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.deleteExpense(expenseUtilities)

        val allExpenses = repository.getAllExpenses().first()
        assertEquals(1, allExpenses.size)
        assertEquals("Loyer boutique", allExpenses[0].title)
    }

    @Test
    fun totalExpensesCalculation_computesSumOfFilteredExpenses() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setSelectedCategory(CATEGORY_ALL)

        val state = viewModel.uiState.value
        assertEquals(125000.0, state.totalExpensesAmount, 0.01)
    }
}
