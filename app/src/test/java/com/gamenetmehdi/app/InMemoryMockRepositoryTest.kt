package com.gamenetmehdi.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryMockRepositoryTest {
    private val repository = InMemoryMockRepository()

    @Test
    fun games_combinesSearchPlatformGenreYearAndRatingFilters() {
        val results = repository.games(
            GameFilter(
                query = "EA Sports",
                platform = "پلی‌استیشن ۵",
                genre = "ورزشی",
                year = 2024,
                minimumRating = 4.5,
            ),
        )

        assertEquals(listOf("fc25"), results.map { it.id })
    }

    @Test
    fun games_normalizesArabicCharactersAndSortsNewestFirst() {
        val results = repository.games(GameFilter(query = "تكن", sort = GameSort.NEWEST))

        assertEquals(listOf("tekken8"), results.map { it.id })
    }

    @Test
    fun games_sortsByRatingDescending() {
        val ratings = repository.games(GameFilter(sort = GameSort.RATING)).map { it.rating }

        assertEquals(ratings.sortedDescending(), ratings)
    }

    @Test
    fun venues_combinesLocationServiceAndAmenityFilters() {
        val results = repository.venues(
            VenueFilter(
                province = "تهران",
                city = "ری",
                service = "تعمیر دستگاه",
                amenity = "کافه",
            ),
        )

        assertEquals(listOf("golbarg"), results.map { it.id })
        assertTrue(results.single().tariffs.isNotEmpty())
        assertTrue(results.single().uniqueGames.isNotEmpty())
    }

    @Test
    fun cities_dependOnProvinceAndChangingProvinceClearsCity() {
        assertEquals(listOf("تهران", "ری"), repository.cities("تهران"))
        assertEquals(listOf("شیراز"), repository.cities("فارس"))

        val changed = VenueFilter(province = "تهران", city = "ری").selectProvince("فارس")
        assertEquals("فارس", changed.province)
        assertEquals(null, changed.city)
    }

    @Test
    fun venues_returnsEmptyForImpossibleCombination() {
        val results = repository.venues(VenueFilter(province = "فارس", amenity = "اینترنت پرسرعت"))

        assertTrue(results.isEmpty())
    }
}
