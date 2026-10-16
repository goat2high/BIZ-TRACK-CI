package com.example.biztrackci.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biztrackci.data.local.entity.ProductEntity
import com.example.biztrackci.data.local.entity.SaleEntity
import com.example.biztrackci.data.local.entity.SaleItemEntity
import com.example.biztrackci.domain.repository.BizTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity
}

data class CompletedSaleReceipt(
    val saleId: Long,
    val timestamp: Long,
    val items: List<CartItem>,
    val totalAmount: Double,
    val paymentMethod: String,
    val staffName: String
)

data class SalesUiState(
    val products: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val selectedPaymentMethod: String = "Espèces",
    val staffName: String = "Kouassi",
    val stockWarningMessage: String? = null,
    val isRecordingSale: Boolean = false,
    val completedSaleReceipt: CompletedSaleReceipt? = null,
    val userMessage: String? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val totalAmount: Double
        get() = subtotal

    val totalCartCount: Int
        get() = cartItems.sumOf { it.quantity }
}

private data class LocalSalesState(
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val selectedPaymentMethod: String = "Espèces",
    val staffName: String = "Kouassi",
    val stockWarningMessage: String? = null,
    val isRecordingSale: Boolean = false,
    val completedSaleReceipt: CompletedSaleReceipt? = null,
    val userMessage: String? = null
)

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: BizTrackRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(LocalSalesState())

    val uiState: StateFlow<SalesUiState> = combine(
        repository.getAllProducts(),
        _localState
    ) { rawProducts, local ->
        val categories = rawProducts.map { it.category }.distinct().sorted()

        val filteredProducts = rawProducts.filter { product ->
            val matchesQuery = local.searchQuery.isBlank() ||
                    product.name.contains(local.searchQuery, ignoreCase = true) ||
                    product.category.contains(local.searchQuery, ignoreCase = true)
            val matchesCategory = local.selectedCategory == null || product.category.equals(local.selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }

        SalesUiState(
            products = rawProducts,
            filteredProducts = filteredProducts,
            categories = categories,
            searchQuery = local.searchQuery,
            selectedCategory = local.selectedCategory,
            cartItems = local.cartItems,
            selectedPaymentMethod = local.selectedPaymentMethod,
            staffName = local.staffName,
            stockWarningMessage = local.stockWarningMessage,
            isRecordingSale = local.isRecordingSale,
            completedSaleReceipt = local.completedSaleReceipt,
            userMessage = local.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalesUiState()
    )

    fun setSearchQuery(query: String) {
        _localState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String?) {
        _localState.update { it.copy(selectedCategory = category) }
    }

    fun selectPaymentMethod(method: String) {
        _localState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setStaffName(name: String) {
        _localState.update { it.copy(staffName = name) }
    }

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        if (product.stockQuantity <= 0) {
            _localState.update {
                it.copy(stockWarningMessage = "Produit '${product.name}' en rupture de stock !")
            }
            return
        }

        _localState.update { current ->
            val cart = current.cartItems.toMutableList()
            val existingIndex = cart.indexOfFirst { it.product.id == product.id }
            var warning: String? = null

            if (existingIndex >= 0) {
                val existingItem = cart[existingIndex]
                val newQty = existingItem.quantity + quantity
                if (newQty > product.stockQuantity) {
                    warning = "Stock insuffisant pour '${product.name}'. Stock disponible : ${product.stockQuantity}"
                    cart[existingIndex] = existingItem.copy(quantity = product.stockQuantity)
                } else {
                    cart[existingIndex] = existingItem.copy(quantity = newQty)
                }
            } else {
                if (quantity > product.stockQuantity) {
                    warning = "Stock insuffisant pour '${product.name}'. Stock disponible : ${product.stockQuantity}"
                    cart.add(CartItem(product = product, quantity = product.stockQuantity))
                } else {
                    cart.add(CartItem(product = product, quantity = quantity))
                }
            }

            current.copy(
                cartItems = cart,
                stockWarningMessage = warning
            )
        }
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        _localState.update { current ->
            val cart = current.cartItems.toMutableList()
            val index = cart.indexOfFirst { it.product.id == productId }
            if (index < 0) return@update current

            var warning: String? = null

            if (newQuantity <= 0) {
                cart.removeAt(index)
                warning = null
            } else {
                val product = cart[index].product
                if (newQuantity > product.stockQuantity) {
                    warning = "Stock insuffisant pour '${product.name}'. Maximum disponible : ${product.stockQuantity}"
                    cart[index] = cart[index].copy(quantity = product.stockQuantity)
                } else {
                    warning = null
                    cart[index] = cart[index].copy(quantity = newQuantity)
                }
            }

            current.copy(
                cartItems = cart,
                stockWarningMessage = warning
            )
        }
    }

    fun removeFromCart(productId: Long) {
        _localState.update { current ->
            current.copy(cartItems = current.cartItems.filter { it.product.id != productId })
        }
    }

    fun clearCart() {
        _localState.update { current ->
            current.copy(cartItems = emptyList(), stockWarningMessage = null)
        }
    }

    fun clearStockWarning() {
        _localState.update { current ->
            current.copy(stockWarningMessage = null)
        }
    }

    fun clearUserMessage() {
        _localState.update { current ->
            current.copy(userMessage = null)
        }
    }

    fun recordSale() {
        val currentState = _localState.value
        val cart = currentState.cartItems
        if (cart.isEmpty()) {
            _localState.update { it.copy(userMessage = "Le panier est vide.") }
            return
        }

        // Validate stock quantities before proceeding
        for (item in cart) {
            if (item.quantity > item.product.stockQuantity) {
                _localState.update {
                    it.copy(stockWarningMessage = "Stock insuffisant pour '${item.product.name}'. Stock disponible : ${item.product.stockQuantity}")
                }
                return
            }
        }

        viewModelScope.launch {
            _localState.update { it.copy(isRecordingSale = true) }
            try {
                val now = System.currentTimeMillis()
                val total = cart.sumOf { it.subtotal }
                val paymentMethod = currentState.selectedPaymentMethod
                val staffName = currentState.staffName

                val saleEntity = SaleEntity(
                    timestamp = now,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    staffName = staffName
                )

                val saleItems = cart.map { cartItem ->
                    SaleItemEntity(
                        saleId = 0,
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.sellingPrice,
                        costPrice = cartItem.product.costPrice
                    )
                }

                val saleId = repository.recordSale(saleEntity, saleItems)

                val receipt = CompletedSaleReceipt(
                    saleId = saleId,
                    timestamp = now,
                    items = cart,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    staffName = staffName
                )

                _localState.update {
                    it.copy(
                        cartItems = emptyList(),
                        stockWarningMessage = null,
                        completedSaleReceipt = receipt,
                        isRecordingSale = false
                    )
                }
            } catch (e: Exception) {
                _localState.update {
                    it.copy(
                        isRecordingSale = false,
                        userMessage = "Erreur lors de l'enregistrement de la vente : ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissReceiptDialog() {
        _localState.update { it.copy(completedSaleReceipt = null) }
    }
}
