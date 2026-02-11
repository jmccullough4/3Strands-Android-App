package com.threestrandscattle.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.screens.*
import com.threestrandscattle.app.ui.theme.ThemeColors

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Menu : Screen("menu", "Menu", Icons.Filled.MenuBook)
    data object FlashSales : Screen("flash_sales", "Flash Sales", Icons.Filled.Bolt)
    data object Inbox : Screen("inbox", "Inbox", Icons.Filled.Notifications)
    data object Events : Screen("events", "Events", Icons.Filled.CalendarMonth)
}

sealed class DetailScreen(val route: String) {
    data object SaleDetail : DetailScreen("sale_detail/{saleId}")
    data object PopUpSales : DetailScreen("popup_sales")
    data object Settings : DetailScreen("settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(store: SaleStore) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Home, Screen.Menu, Screen.FlashSales, Screen.Inbox, Screen.Events)

    val activeSalesCount by remember { derivedStateOf { store.activeSales.size } }
    val unreadCount by remember { derivedStateOf { store.unreadCount } }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ThemeColors.Background,
                contentColor = ThemeColors.Primary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    when (screen) {
                                        Screen.FlashSales -> {
                                            if (activeSalesCount > 0) {
                                                Badge(
                                                    containerColor = ThemeColors.Primary
                                                ) {
                                                    Text("$activeSalesCount")
                                                }
                                            }
                                        }
                                        Screen.Inbox -> {
                                            if (unreadCount > 0) {
                                                Badge(
                                                    containerColor = ThemeColors.Primary
                                                ) {
                                                    Text("$unreadCount")
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            ) {
                                Icon(screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ThemeColors.Primary,
                            selectedTextColor = ThemeColors.Primary,
                            unselectedIconColor = ThemeColors.TextSecondary,
                            unselectedTextColor = ThemeColors.TextSecondary,
                            indicatorColor = ThemeColors.Primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    store = store,
                    onNavigateToSettings = { navController.navigate(DetailScreen.Settings.route) },
                    onNavigateToSaleDetail = { saleId ->
                        navController.navigate("sale_detail/$saleId")
                    },
                    onNavigateToPopUpSales = { navController.navigate(DetailScreen.PopUpSales.route) }
                )
            }
            composable(Screen.Menu.route) {
                MenuScreen(store = store)
            }
            composable(Screen.FlashSales.route) {
                FlashSalesScreen(
                    store = store,
                    onNavigateToSaleDetail = { saleId ->
                        navController.navigate("sale_detail/$saleId")
                    }
                )
            }
            composable(Screen.Inbox.route) {
                NotificationInboxScreen(store = store)
            }
            composable(Screen.Events.route) {
                EventsScreen(store = store)
            }
            composable(
                route = DetailScreen.SaleDetail.route,
                arguments = listOf(navArgument("saleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                val sale = store.sales.collectAsState().value.find { it.id == saleId }
                if (sale != null) {
                    SaleDetailScreen(sale = sale, onBack = { navController.popBackStack() })
                }
            }
            composable(DetailScreen.PopUpSales.route) {
                PopUpSaleScreen(store = store, onBack = { navController.popBackStack() })
            }
            composable(DetailScreen.Settings.route) {
                SettingsScreen(store = store, onBack = { navController.popBackStack() })
            }
        }
    }
}
