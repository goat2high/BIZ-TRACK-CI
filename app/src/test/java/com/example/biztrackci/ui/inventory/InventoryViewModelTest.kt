package com.example.biztrackci.ui.inventory

import com.example.biztrackci.data.local.entity.ProductEntity
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
class InventoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBizTrackRepository
    private lateinit var viewModel: InventoryViewModel

    private val normalProduct = ProductEntity(
        id = 1,
        name = "Riz Uncle Sam 5kg",
        category = "Alimentation",
        costPrice = 3500.0,
        sellingPrice = 4500.0,
        stockQuantity = 20,
        minStockThreshold = 5,
        unit = "Sac"
    )

    private val lowStockProduct = ProductEntity(
        id = 2,
        name = "Sucre Blanc 1kg",
        category = "Alimentation",
        costPrice = 800.0,
        sellingPrice = 950.0,
        stockQuantity = 2, // Stock <= minStockThreshold (5)
        minStockThreshold = 5,
        unit = "Sachet"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBizTrackRepository()
        repository.productsFlow.value = listOf(normalProduct, lowStockProduct)
        viewModel = InventoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filter_lowStock_returnsOnlyLowStockProducts() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.setSelectedFilter(InventoryFilter.LOW_STOCK)

        val state = viewModel.uiState.value
        assertEquals(2, state.totalProductsCount)
        assertEquals(1, state.lowStockCount)
        assertEquals(1, state.filteredProducts.size)
        assertEquals("Sucre Blanc 1kg", state.filteredProducts[0].name)
    }

    @Test
    fun addProduct_validInput_insertsIntoRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.addProduct(
            name = "Huile Dinor 1L",
            category = "Alimentation",
            costPrice = 1100.0,
            sellingPrice = 1400.0,
            initialStock = 15,
            minStockThreshold = 5,
            unit = "Bouteille"
        )

        val allProducts = repository.getAllProducts().first()
        assertEquals(3, allProducts.size)
        assertTrue(allProducts.any { it.name == "Huile Dinor 1L" })
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun updateStockQuantity_updatesProductInRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.updateStockQuantity(normalProduct.id, 50)

        val updated = repository.getProductById(normalProduct.id).first()
        assertEquals(50, updated?.stockQuantity)
    }

    @Test
    fun deleteProduct_removesFromRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.deleteProduct(lowStockProduct)

        val allProducts = repository.getAllProducts().first()
        assertEquals(1, allProducts.size)
        assertEquals("Riz Uncle Sam 5kg", allProducts[0].name)
    }
}
