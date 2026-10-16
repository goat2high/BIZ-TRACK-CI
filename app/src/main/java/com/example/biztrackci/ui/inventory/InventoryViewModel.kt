package com.example.biztrackci.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biztrackci.data.local.entity.ProductEntity
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

enum class InventoryFilter {
    ALL, LOW_STOCK
}

data class InventoryUiState(
    val products: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val searchQuery: String = "",
    val isAddProductDialogOpen: Boolean = false,
    val productForStockAdjustment: ProductEntity? = null,
    val productForEdit: ProductEntity? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

private data class LocalInventoryState(
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val searchQuery: String = "",
    val isAddProductDialogOpen: Boolean = false,
    val productForStockAdjustment: ProductEntity? = null,
    val productForEdit: ProductEntity? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: BizTrackRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(LocalInventoryState())

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.getAllProducts(),
        repository.getLowStockProducts(),
        _localState
    ) { allProducts, lowStockProducts, local ->

        val totalCount = allProducts.size
        val lowStockCount = lowStockProducts.size

        val baseList = when (local.selectedFilter) {
            InventoryFilter.ALL -> allProducts
            InventoryFilter.LOW_STOCK -> lowStockProducts
        }

        val filtered = baseList.filter { product ->
            local.searchQuery.isBlank() ||
                    product.name.contains(local.searchQuery, ignoreCase = true) ||
                    product.category.contains(local.searchQuery, ignoreCase = true)
        }

        InventoryUiState(
            products = allProducts,
            filteredProducts = filtered,
            totalProductsCount = totalCount,
            lowStockCount = lowStockCount,
            selectedFilter = local.selectedFilter,
            searchQuery = local.searchQuery,
            isAddProductDialogOpen = local.isAddProductDialogOpen,
            productForStockAdjustment = local.productForStockAdjustment,
            productForEdit = local.productForEdit,
            isLoading = local.isLoading,
            userMessage = local.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun setSelectedFilter(filter: InventoryFilter) {
        _localState.update { it.copy(selectedFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _localState.update { it.copy(searchQuery = query) }
    }

    fun openAddProductDialog() {
        _localState.update { it.copy(isAddProductDialogOpen = true) }
    }

    fun closeAddProductDialog() {
        _localState.update { it.copy(isAddProductDialogOpen = false) }
    }

    fun openStockAdjustmentDialog(product: ProductEntity) {
        _localState.update { it.copy(productForStockAdjustment = product) }
    }

    fun closeStockAdjustmentDialog() {
        _localState.update { it.copy(productForStockAdjustment = null) }
    }

    fun openEditProductDialog(product: ProductEntity) {
        _localState.update { it.copy(productForEdit = product) }
    }

    fun closeEditProductDialog() {
        _localState.update { it.copy(productForEdit = null) }
    }

    fun clearUserMessage() {
        _localState.update { it.copy(userMessage = null) }
    }

    fun addProduct(
        name: String,
        category: String,
        costPrice: Double,
        sellingPrice: Double,
        initialStock: Int,
        minStockThreshold: Int,
        unit: String
    ) {
        if (name.isBlank()) {
            _localState.update { it.copy(userMessage = "Le nom du produit est obligatoire.") }
            return
        }
        if (category.isBlank()) {
            _localState.update { it.copy(userMessage = "La catégorie est obligatoire.") }
            return
        }
        if (unit.isBlank()) {
            _localState.update { it.copy(userMessage = "L'unité est obligatoire.") }
            return
        }
        if (costPrice < 0 || sellingPrice < 0) {
            _localState.update { it.copy(userMessage = "Les prix doivent être positifs.") }
            return
        }

        viewModelScope.launch {
            try {
                val newProduct = ProductEntity(
                    name = name.trim(),
                    category = category.trim(),
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    stockQuantity = initialStock.coerceAtLeast(0),
                    minStockThreshold = minStockThreshold.coerceAtLeast(0),
                    unit = unit.trim()
                )
                repository.addProduct(newProduct)
                _localState.update {
                    it.copy(
                        isAddProductDialogOpen = false,
                        userMessage = "Produit '$name' ajouté avec succès !"
                    )
                }
            } catch (e: Exception) {
                _localState.update {
                    it.copy(userMessage = "Erreur lors de l'ajout du produit : ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateProduct(product: ProductEntity) {
        if (product.name.isBlank()) {
            _localState.update { it.copy(userMessage = "Le nom du produit ne peut pas être vide.") }
            return
        }
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _localState.update {
                    it.copy(
                        productForEdit = null,
                        userMessage = "Produit mis à jour avec succès !"
                    )
                }
            } catch (e: Exception) {
                _localState.update {
                    it.copy(userMessage = "Erreur lors de la mise à jour : ${e.localizedMessage}")
                }
            }
        }
    }

    fun adjustStockQuantity(productId: Long, currentStock: Int, delta: Int) {
        val newQuantity = (currentStock + delta).coerceAtLeast(0)
        updateStockQuantity(productId, newQuantity)
    }

    fun updateStockQuantity(productId: Long, newQuantity: Int) {
        val quantityToSet = newQuantity.coerceAtLeast(0)
        viewModelScope.launch {
            try {
                repository.updateStockQuantity(productId, quantityToSet)
                _localState.update {
                    it.copy(
                        productForStockAdjustment = null,
                        userMessage = "Stock mis à jour avec succès !"
                    )
                }
            } catch (e: Exception) {
                _localState.update {
                    it.copy(userMessage = "Erreur lors de l'ajustement du stock : ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _localState.update {
                    it.copy(userMessage = "Produit '${product.name}' supprimé.")
                }
            } catch (e: Exception) {
                _localState.update {
                    it.copy(userMessage = "Erreur lors de la suppression : ${e.localizedMessage}")
                }
            }
        }
    }
}
