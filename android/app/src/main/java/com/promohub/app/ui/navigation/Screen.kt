package com.promohub.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Detail : Screen("detail/{promocodeId}") {
        fun createRoute(promocodeId: Int) = "detail/$promocodeId"
    }
    object Favorites : Screen("favorites")
    object History : Screen("history")
    object Profile : Screen("profile")
}
