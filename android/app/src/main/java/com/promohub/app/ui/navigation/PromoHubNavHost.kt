package com.promohub.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.promohub.app.data.local.AppDatabase
import com.promohub.app.data.local.TokenManager
import com.promohub.app.data.remote.RetrofitClient
import com.promohub.app.data.repository.PromoRepository
import com.promohub.app.domain.viewmodel.AuthViewModel
import com.promohub.app.domain.viewmodel.PromoViewModel
import com.promohub.app.ui.screens.*

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun PromoHubNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val database = remember { AppDatabase.getDatabase(context) }
    val tokenManager = remember { TokenManager(context) }
    val repository = remember {
        PromoRepository(RetrofitClient.api, database.promoCodeDao(), tokenManager)
    }

    val authViewModel = remember { AuthViewModel(repository) }
    val promoViewModel = remember { PromoViewModel(repository) }

    val authState by authViewModel.state.collectAsState()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "Главная", Icons.Default.Home),
        BottomNavItem(Screen.Favorites, "Избранное", Icons.Default.Favorite),
        BottomNavItem(Screen.History, "История", Icons.Default.History),
        BottomNavItem(Screen.Profile, "Профиль", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onDone = {
                    val dest = if (authViewModel.state.value.isAuthenticated)
                        Screen.Home.route else Screen.Login.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    promoViewModel = promoViewModel,
                    onPromoCodeClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("promocodeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val promocodeId = backStackEntry.arguments?.getInt("promocodeId") ?: 0
                val promoCode = promoViewModel.state.collectAsState().value.promocodes.find { it.id == promocodeId }
                promoCode?.let {
                    DetailScreen(
                        promoCode = it,
                        promoViewModel = promoViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    promoViewModel = promoViewModel,
                    onPromoCodeClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    promoViewModel = promoViewModel,
                    onPromoCodeClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(authViewModel = authViewModel)
            }
        }
    }
}
