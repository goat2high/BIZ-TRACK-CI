package com.example.biztrackci.ui.profile

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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeBizTrackRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBizTrackRepository()
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateRole_switchesUserRoleInRepositoryAndState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertEquals("OWNER", viewModel.uiState.value.userRole)

        viewModel.updateRole("STAFF")

        assertEquals("STAFF", repository.userRole.first())
        assertEquals("STAFF", viewModel.uiState.value.userRole)
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun updateBusinessName_updatesNameInRepositoryAndState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.updateBusinessName("Supermarché Treichville")

        assertEquals("Supermarché Treichville", repository.businessName.first())
        assertEquals("Supermarché Treichville", viewModel.uiState.value.businessName)
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun updateSubscriptionStatus_updatesStatusInRepositoryAndState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.updateSubscriptionStatus("TRIAL")

        assertEquals("TRIAL", repository.subscriptionStatus.first())
        assertEquals("TRIAL", viewModel.uiState.value.subscriptionStatus)
        assertNotNull(viewModel.uiState.value.userMessage)
    }
}
