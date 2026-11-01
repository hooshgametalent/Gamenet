package com.gamenetmehdi.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import java.net.URLDecoder
import java.net.URLEncoder

private const val NULL_STATE_VALUE = "\u0000"
private const val DETAIL_SEPARATOR = "\n"

enum class MainDestination(val label: String, val symbol: String) {
    HOME("خانه", "⌂"),
    GAMES("بازی‌ها", "🎮"),
    VENUES("گیمنت‌ها", "▣"),
    MAP("نقشه", "⌖"),
    ACCOUNT("ورود", "●"),
}

enum class CatalogLayout { LIST, GRID }

sealed interface AppRoute {
    data class Main(val destination: MainDestination) : AppRoute
    data class GameDetail(val id: String) : AppRoute
    data class VenueDetail(val id: String) : AppRoute
    data object Services : AppRoute
    data class ServiceDetail(val id: String) : AppRoute
}

internal data class AppStateSnapshot(
    val route: AppRoute,
    val selectedDestination: MainDestination,
    val details: List<AppRoute>,
    val homeGameQuery: String,
    val gameFilter: GameFilter,
    val gameLayout: CatalogLayout,
    val gamePage: Int,
    val venueFilter: VenueFilter,
    val mapProvince: String?,
    val mapLayer: String,
    val accountMode: AuthMode,
)

class AppNavigator internal constructor(initialDestination: MainDestination) {
    var route: AppRoute by mutableStateOf(AppRoute.Main(initialDestination))
        private set
    var selectedDestination: MainDestination by mutableStateOf(initialDestination)
        private set
    var homeGameQuery: String by mutableStateOf("")
    var gameFilter: GameFilter by mutableStateOf(GameFilter())
    var gameLayout: CatalogLayout by mutableStateOf(CatalogLayout.LIST)
    var gamePage: Int by mutableIntStateOf(0)
    var venueFilter: VenueFilter by mutableStateOf(VenueFilter())
    var mapProvince: String? by mutableStateOf(null)
    var mapLayer: String by mutableStateOf("ساده")
    var accountMode: AuthMode by mutableStateOf(AuthMode.LOGIN)
    private val details = mutableListOf<AppRoute>()

    internal constructor(snapshot: AppStateSnapshot) : this(snapshot.selectedDestination) {
        route = snapshot.route
        details += snapshot.details
        homeGameQuery = snapshot.homeGameQuery
        gameFilter = snapshot.gameFilter
        gameLayout = snapshot.gameLayout
        gamePage = snapshot.gamePage.coerceAtLeast(0)
        venueFilter = snapshot.venueFilter
        mapProvince = snapshot.mapProvince
        mapLayer = snapshot.mapLayer
        accountMode = snapshot.accountMode
    }

    fun openMain(destination: MainDestination) {
        selectedDestination = destination
        details.clear()
        route = AppRoute.Main(destination)
    }

    fun openGames(query: String) {
        selectedDestination = MainDestination.GAMES
        gameFilter = gameFilter.copy(query = query.trim())
        gamePage = 0
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

    internal fun snapshot(): AppStateSnapshot = AppStateSnapshot(
        route = route,
        selectedDestination = selectedDestination,
        details = details.toList(),
        homeGameQuery = homeGameQuery,
        gameFilter = gameFilter,
        gameLayout = gameLayout,
        gamePage = gamePage,
        venueFilter = venueFilter,
        mapProvince = mapProvince,
        mapLayer = mapLayer,
        accountMode = accountMode,
    )

    internal fun savedValues(): List<String> = snapshot().toSavedValues()

    companion object {
        internal fun restoreSavedValues(values: List<String>): AppNavigator = AppNavigator(values.toAppStateSnapshot())

        val Saver: Saver<AppNavigator, Any> = listSaver<AppNavigator, String>(
            save = { navigator -> navigator.savedValues() },
            restore = { values -> restoreSavedValues(values) },
        )
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator = rememberSaveable(saver = AppNavigator.Saver) {
    AppNavigator(MainDestination.HOME)
}

private fun AppStateSnapshot.toSavedValues(): List<String> = listOf(
    route.encode(),
    selectedDestination.name,
    details.joinToString(DETAIL_SEPARATOR, transform = AppRoute::encode),
    homeGameQuery,
    gameFilter.query,
    gameFilter.platform.orNullMarker(),
    gameFilter.genre.orNullMarker(),
    gameFilter.year?.toString().orNullMarker(),
    gameFilter.minimumRating?.toString().orNullMarker(),
    gameFilter.sort.name,
    gameLayout.name,
    gamePage.toString(),
    venueFilter.query,
    venueFilter.province.orNullMarker(),
    venueFilter.city.orNullMarker(),
    venueFilter.service.orNullMarker(),
    venueFilter.amenity.orNullMarker(),
    mapProvince.orNullMarker(),
    mapLayer,
    accountMode.name,
)

private fun List<String>.toAppStateSnapshot(): AppStateSnapshot {
    val selected = enumValueOrDefault(getOrNull(1), MainDestination.HOME)
    return AppStateSnapshot(
        route = getOrNull(0)?.decodeRoute() ?: AppRoute.Main(selected),
        selectedDestination = selected,
        details = getOrNull(2).orEmpty().split(DETAIL_SEPARATOR).mapNotNull(String::decodeRoute),
        homeGameQuery = getOrNull(3).orEmpty(),
        gameFilter = GameFilter(
            query = getOrNull(4).orEmpty(),
            platform = getOrNull(5).fromNullMarker(),
            genre = getOrNull(6).fromNullMarker(),
            year = getOrNull(7).fromNullMarker()?.toIntOrNull(),
            minimumRating = getOrNull(8).fromNullMarker()?.toDoubleOrNull(),
            sort = enumValueOrDefault(getOrNull(9), GameSort.RECOMMENDED),
        ),
        gameLayout = enumValueOrDefault(getOrNull(10), CatalogLayout.LIST),
        gamePage = getOrNull(11)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
        venueFilter = VenueFilter(
            query = getOrNull(12).orEmpty(),
            province = getOrNull(13).fromNullMarker(),
            city = getOrNull(14).fromNullMarker(),
            service = getOrNull(15).fromNullMarker(),
            amenity = getOrNull(16).fromNullMarker(),
        ),
        mapProvince = getOrNull(17).fromNullMarker(),
        mapLayer = getOrNull(18).takeUnless { it.isNullOrBlank() } ?: "ساده",
        accountMode = enumValueOrDefault(getOrNull(19), AuthMode.LOGIN),
    )
}

private fun AppRoute.encode(): String = when (this) {
    is AppRoute.Main -> "main:${destination.name}"
    is AppRoute.GameDetail -> "game:${id.urlEncode()}"
    is AppRoute.VenueDetail -> "venue:${id.urlEncode()}"
    AppRoute.Services -> "services"
    is AppRoute.ServiceDetail -> "service:${id.urlEncode()}"
}

private fun String.decodeRoute(): AppRoute? {
    val type = substringBefore(':')
    val value = substringAfter(':', missingDelimiterValue = "")
    return when (type) {
        "main" -> AppRoute.Main(enumValueOrDefault(value, MainDestination.HOME))
        "game" -> value.urlDecode().takeIf(String::isNotBlank)?.let(AppRoute::GameDetail)
        "venue" -> value.urlDecode().takeIf(String::isNotBlank)?.let(AppRoute::VenueDetail)
        "services" -> AppRoute.Services
        "service" -> value.urlDecode().takeIf(String::isNotBlank)?.let(AppRoute::ServiceDetail)
        else -> null
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    runCatching { enumValueOf<T>(value.orEmpty()) }.getOrDefault(default)

private fun String?.orNullMarker(): String = this ?: NULL_STATE_VALUE
private fun String?.fromNullMarker(): String? = this?.takeUnless { it == NULL_STATE_VALUE }
private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())
private fun String.urlDecode(): String = runCatching { URLDecoder.decode(this, Charsets.UTF_8.name()) }.getOrDefault("")
