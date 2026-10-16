package com.example.biztrackci.ui.sales

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBizTrackRepository
    private lateinit var viewModel: SalesViewModel

    private val sampleProduct = ProductEntity(
        id = 1,
        name = "Riz Uncle Sam 5kg",
        category = "Alimentation",
        costPrice = 3500.0,
        sellingPrice = 4500.0,
        stockQuantity = 10,
        minStockThreshold = 2,
        unit = "Sac"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBizTrackRepository()
        repository.productsFlow.value = listOf(sampleProduct)
        viewModel = SalesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addToCart_validQuantity_updatesCartAndSubtotal() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.addToCart(sampleProduct, 2)

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(2, state.cartItems[0].quantity)
        assertEquals(9000.0, state.subtotal, 0.01)
        assertNull(state.stockWarningMessage)
    }

    @Test
    fun addToCart_exceedingStock_setsStockWarning() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        // Attempting to add 15 items when only 10 in stock
        viewModel.addToCart(sampleProduct, 15)

        val state = viewModel.uiState.value
        assertEquals(1, state.cartItems.size)
        assertEquals(10, state.cartItems[0].quantity) // capped at max 10
        assertNotNull(state.stockWarningMessage)
        assertTrue(state.stockWarningMessage?.contains("Stock insuffisant") == true)
    }

    @Test
    fun updateCartQuantity_andRemove_worksCorrectly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.addToCart(sampleProduct, 5)
        assertEquals(5, viewModel.uiState.value.cartItems[0].quantity)

        viewModel.updateCartQuantity(sampleProduct.id, 3)
        assertEquals(3, viewModel.uiState.value.cartItems[0].quantity)

        // Reduce to 0 removes from cart
        viewModel.updateCartQuantity(sampleProduct.id, 0)
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun selectPaymentMethod_updatesState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.selectPaymentMethod("Wave")

        assertEquals("Wave", viewModel.uiState.value.selectedPaymentMethod)
    }

    @Test
    fun recordSale_success_clearsCartAndGeneratesReceipt() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.addToCart(sampleProduct, 2)
        viewModel.selectPaymentMethod("Orange Money")

        viewModel.recordSale()

        val state = viewModel.uiState.value
        assertTrue(state.cartItems.isEmpty())
        assertNotNull(state.completedSaleReceipt)
        assertEquals(9000.0, state.completedSaleReceipt?.totalAmount ?: 0.0, 0.01)
        assertEquals("Orange Money", state.completedSaleReceipt?.paymentMethod)

        // Verify stock in repository reduced from 10 to 8
        val updatedProduct = repository.getProductById(1).first()
        assertEquals(8, updatedProduct?.stockQuantity)
    }
}
