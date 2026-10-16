package com.example.biztrackci.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.biztrackci.ui.analytics.AnalyticsScreen
import com.example.biztrackci.ui.analytics.AnalyticsViewModel
import com.example.biztrackci.ui.expenses.ExpensesScreen
import com.example.biztrackci.ui.expenses.ExpensesViewModel
import com.example.biztrackci.ui.inventory.InventoryScreen
import com.example.biztrackci.ui.inventory.InventoryViewModel
import com.example.biztrackci.ui.profile.ProfileScreen
import com.example.biztrackci.ui.profile.ProfileViewModel
import com.example.biztrackci.ui.sales.SalesScreen
import com.example.biztrackci.ui.sales.SalesViewModel

enum class NavDestination(
    val title: String,
    val icon: ImageVector
) {
    SALES("Ventes", Icons.Rounded.ShoppingCart),
    INVENTORY("Stock", Icons.Rounded.Inventory2),
    EXPENSES("Dépenses", Icons.AutoMirrored.Rounded.ReceiptLong),
    ANALYTICS("Analytics", Icons.Rounded.BarChart),
    PROFILE("Profil", Icons.Rounded.Person)
}

@Composable
fun BizTrackApp(
    modifier: Modifier = Modifier,
    salesViewModel: SalesViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    expensesViewModel: ExpensesViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var currentDestination by rememberSaveable { mutableStateOf(NavDestination.SALES) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { destination ->
            when (destination) {
                NavDestination.SALES -> SalesScreen(viewModel = salesViewModel)
                NavDestination.INVENTORY -> InventoryScreen(viewModel = inventoryViewModel)
                NavDestination.EXPENSES -> ExpensesScreen(viewModel = expensesViewModel)
                NavDestination.ANALYTICS -> AnalyticsScreen(viewModel = analyticsViewModel)
                NavDestination.PROFILE -> ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
