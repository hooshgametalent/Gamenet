package com.gamenetmehdi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class MainDestination(val label: String, val symbol: String) {
    HOME("خانه", "⌂"),
    GAMES("بازی‌ها", "🎮"),
    VENUES("گیمنت‌ها", "▣"),
    MAP("نقشه", "⌖"),
    ACCOUNT("ورود", "●"),
}

sealed interface AppRoute {
    data class Main(val destination: MainDestination) : AppRoute
    data class GameDetail(val id: String) : AppRoute
    data class VenueDetail(val id: String) : AppRoute
    data object Services : AppRoute
    data class ServiceDetail(val id: String) : AppRoute
}

class AppNavigator internal constructor(initialDestination: MainDestination) {
    var route: AppRoute by mutableStateOf(AppRoute.Main(initialDestination))
        private set
    var selectedDestination: MainDestination by mutableStateOf(initialDestination)
        private set
    var initialGameQuery: String by mutableStateOf("")
        private set
    private val details = mutableListOf<AppRoute>()

    fun openMain(destination: MainDestination) {
        selectedDestination = destination
        if (destination == MainDestination.GAMES) initialGameQuery = ""
        details.clear()
        route = AppRoute.Main(destination)
    }

    fun openGames(query: String) {
        selectedDestination = MainDestination.GAMES
        initialGameQuery = query.trim()
        details.clear()
        route = AppRoute.Main(MainDestination.GAMES)
    }

    fun open(route: AppRoute) {
        details += this.route
        this.route = route
    }

    fun back(): Boolean {
        val previous = details.removeLastOrNull() ?: return false
        route = previous
        if (previous is AppRoute.Main) selectedDestination = previous.destination
        return true
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator = remember { AppNavigator(MainDestination.HOME) }
