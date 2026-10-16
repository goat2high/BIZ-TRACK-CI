package com.example.biztrackci.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biztrackci.data.local.entity.ExpenseEntity
import com.example.biztrackci.domain.repository.BizTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpensesUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val selectedCategory: String = CATEGORY_ALL,
    val searchQuery: String = "",
    val totalExpensesAmount: Double = 0.0,
    val userRole: String = "OWNER",
    val isAddExpenseDialogOpen: Boolean = false,
    val expenseToEdit: ExpenseEntity? = null,
    val userMessage: String? = null
)

const val CATEGORY_ALL = "Toutes"
val EXPENSE_CATEGORIES = listOf(
    "Loyer",
    "Services",
    "Salaires",
    "Fournisseurs",
    "Transport",
    "Autres"
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: BizTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CATEGORY_ALL)
    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                repository.getAllExpenses(),
                _selectedCategory,
                _searchQuery,
                repository.userRole
            ) { allExpenses, category, query, role ->
                val filtered = allExpenses.filter { expense ->
                    val matchesCategory = (category == CATEGORY_ALL) || expense.category.equals(category, ignoreCase = true)
                    val matchesQuery = query.isBlank() ||
                            expense.title.contains(query, ignoreCase = true) ||
                            (expense.notes?.contains(query, ignoreCase = true) == true)
                    matchesCategory && matchesQuery
                }
                val totalAmount = filtered.sumOf { it.amount }

                ExpensesUiState(
                    expenses = allExpenses,
                    filteredExpenses = filtered,
                    selectedCategory = category,
                    searchQuery = query,
                    totalExpensesAmount = totalAmount,
                    userRole = role,
                    isAddExpenseDialogOpen = _uiState.value.isAddExpenseDialogOpen,
                    expenseToEdit = _uiState.value.expenseToEdit,
                    userMessage = _uiState.value.userMessage
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAddExpenseDialog(expenseToEdit: ExpenseEntity? = null) {
        _uiState.update {
            it.copy(
                isAddExpenseDialogOpen = true,
                expenseToEdit = expenseToEdit
            )
        }
    }

    fun closeAddExpenseDialog() {
        _uiState.update {
            it.copy(
                isAddExpenseDialogOpen = false,
                expenseToEdit = null
            )
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        notes: String?,
        receiptImagePath: String? = null
    ) {
        if (title.isBlank() || amount <= 0) {
            _uiState.update { it.copy(userMessage = "Veuillez saisir un titre valide et un montant supérieur à 0") }
            return
        }

        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title.trim(),
                amount = amount,
                category = category,
                timestamp = System.currentTimeMillis(),
                receiptImagePath = receiptImagePath,
                notes = notes?.trim()
            )
            repository.addExpense(expense)
            _uiState.update {
                it.copy(
                    isAddExpenseDialogOpen = false,
                    userMessage = "Dépense enregistrée avec succès"
                )
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            _uiState.update {
                it.copy(
                    isAddExpenseDialogOpen = false,
                    expenseToEdit = null,
                    userMessage = "Dépense mise à jour"
                )
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _uiState.update { it.copy(userMessage = "Dépense supprimée") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
