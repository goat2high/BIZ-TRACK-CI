package com.example.biztrackci.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.domain.model.FinancialSummary
import com.example.biztrackci.domain.repository.BizTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeRange(val label: String) {
    TODAY("Aujourd'hui"),
    THIS_WEEK("Cette Semaine"),
    THIS_MONTH("Ce Mois"),
    ALL_TIME("Tout")
}

data class PaymentMethodStat(
    val methodName: String,
    val amount: Double,
    val percentage: Float
)

data class AnalyticsUiState(
    val selectedTimeRange: TimeRange = TimeRange.THIS_MONTH,
    val financialSummary: FinancialSummary = FinancialSummary(0.0, 0.0, 0.0, 0.0),
    val paymentMethodStats: List<PaymentMethodStat> = emptyList(),
    val lowStockProductsCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val userRole: String = "OWNER",
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: BizTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.THIS_MONTH)

    init {
        viewModelScope.launch {
            combine(
                _selectedTimeRange,
                repository.userRole,
                repository.getLowStockProducts()
            ) { timeRange, role, lowStockList ->
                Triple(timeRange, role, lowStockList)
            }.flatMapLatest { (timeRange, role, lowStockList) ->
                val (startDate, endDate) = getTimeRangeDates(timeRange)
                combine(
                    repository.getFinancialSummaryBetween(startDate, endDate),
                    repository.getSalesBetweenDates(startDate, endDate)
                ) { summary, salesWithItems ->
                    val totalRevenue = summary.totalRevenue

                    // Payment Methods Breakdown
                    val standardMethods = listOf("Wave", "Orange Money", "MTN Mobile Money", "Espèces")
                    val salesByMethod = salesWithItems.groupBy { it.sale.paymentMethod }

                    val methodStats = standardMethods.map { method ->
                        val amount = salesByMethod[method]?.sumOf { it.sale.totalAmount } ?: 0.0
                        val percentage = if (totalRevenue > 0) (amount / totalRevenue * 100).toFloat() else 0f
                        PaymentMethodStat(
                            methodName = method,
                            amount = amount,
                            percentage = percentage
                        )
                    }

                    AnalyticsUiState(
                        selectedTimeRange = timeRange,
                        financialSummary = summary,
                        paymentMethodStats = methodStats,
                        lowStockProductsCount = lowStockList.size,
                        lowStockProducts = lowStockList,
                        userRole = role,
                        isLoading = false
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }

    companion object {
        fun getTimeRangeDates(timeRange: TimeRange): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis

            val startDate = when (timeRange) {
                TimeRange.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                TimeRange.THIS_WEEK -> {
                    calendar.firstDayOfWeek = Calendar.MONDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                TimeRange.THIS_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                TimeRange.ALL_TIME -> 0L
            }
            return Pair(startDate, endDate)
        }
    }
}
