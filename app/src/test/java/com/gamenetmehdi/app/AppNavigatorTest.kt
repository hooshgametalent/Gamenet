package com.gamenetmehdi.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {
    @Test
    fun bottomDestinations_keepRequiredMobileOrder() {
        assertEquals(
            listOf("خانه", "بازی‌ها", "گیمنت‌ها", "نقشه", "ورود"),
            MainDestination.entries.map { it.label },
        )
    }

    @Test
    fun back_walksNestedDetailHistory() {
        val navigator = AppNavigator(MainDestination.HOME)
        navigator.openMain(MainDestination.GAMES)
        navigator.open(AppRoute.GameDetail("tekken8"))
        navigator.open(AppRoute.VenueDetail("golbarg"))

        assertTrue(navigator.back())
        assertEquals(AppRoute.GameDetail("tekken8"), navigator.route)
        assertTrue(navigator.back())
        assertEquals(AppRoute.Main(MainDestination.GAMES), navigator.route)
        assertEquals(MainDestination.GAMES, navigator.selectedDestination)
        assertFalse(navigator.back())
    }

    @Test
    fun selectingBottomDestination_clearsDetailHistory() {
        val navigator = AppNavigator(MainDestination.HOME)
        navigator.open(AppRoute.GameDetail("fc25"))

        navigator.openMain(MainDestination.VENUES)

        assertEquals(AppRoute.Main(MainDestination.VENUES), navigator.route)
        assertEquals(MainDestination.VENUES, navigator.selectedDestination)
        assertFalse(navigator.back())
    }

    @Test
    fun homeSearch_handsTrimmedQueryToGamesAndResetsPage() {
        val navigator = AppNavigator(MainDestination.HOME)
        navigator.gamePage = 2

        navigator.openGames("  تکن ۸  ")

        assertEquals(AppRoute.Main(MainDestination.GAMES), navigator.route)
        assertEquals("تکن ۸", navigator.gameFilter.query)
        assertEquals(0, navigator.gamePage)
    }

    @Test
    fun savedValues_restoreNavigationAndNonSensitiveCatalogState() {
        val navigator = AppNavigator(MainDestination.HOME).apply {
            homeGameQuery = "مورتال"
            gameFilter = GameFilter("تکن", "پلی‌استیشن ۵", "مبارزه‌ای", 2024, 4.5, GameSort.RATING)
            gameLayout = CatalogLayout.GRID
            gamePage = 1
            venueFilter = VenueFilter("گلبرگ", "تهران", "ری", "تعمیر دسته", "کافه")
            mapProvince = "فارس"
            mapLayer = "تیره"
            accountMode = AuthMode.REGISTER
            openMain(MainDestination.GAMES)
            open(AppRoute.GameDetail("game:id/نمونه"))
            open(AppRoute.VenueDetail("venue:id/نمونه"))
        }

        val restored = AppNavigator.restoreSavedValues(navigator.savedValues())

        assertEquals(navigator.snapshot(), restored.snapshot())
    }
}
