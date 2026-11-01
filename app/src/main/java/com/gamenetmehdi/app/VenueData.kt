package com.gamenetmehdi.app

data class Device(
    val name: String,
    val type: String,
    val activeInSystem: Boolean = true,
    val inUse: Boolean = false,
)

data class Room(
    val name: String,
    val devices: List<Device>,
) {
    val activeDeviceCount: Int get() = devices.count { it.activeInSystem }
}

data class Tariff(
    val deviceType: String,
    val hourlyPrice: String,
)

data class Venue(
    val id: String,
    val name: String,
    val badge: String,
    val province: String,
    val city: String,
    val phone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val services: List<String>,
    val amenities: List<String>,
    val rooms: List<Room>,
    val uniqueGames: List<String>,
    val tariffs: List<Tariff>,
    val featured: Boolean = false,
) {
    val location: String get() = "$province، $city"
    val roomCount: Int get() = rooms.size
    val deviceCount: Int get() = rooms.sumOf { it.devices.size }
    val activeDeviceCount: Int get() = rooms.sumOf { it.activeDeviceCount }
    val deviceTypes: List<String> get() = rooms.flatMap { it.devices }.map { it.type }.distinct()
}

data class Game(
    val id: String,
    val title: String,
    val latinTitle: String,
    val description: String,
    val releaseYear: Int,
    val ageRating: String,
    val rating: Double,
    val platforms: List<String>,
    val genres: List<String>,
    val galleryLabels: List<String>,
    val officialUrl: String?,
    val relatedGameIds: List<String>,
    val venueIds: List<String>,
    val featured: Boolean = false,
)

data class TechnicalService(
    val id: String,
    val title: String,
    val shortDescription: String,
    val description: String,
    val steps: List<String>,
    val venueIds: List<String>,
)

data class PlatformStatistic(
    val platform: String,
    val gameCount: Int,
)

data class HomeContent(
    val headline: String,
    val subtitle: String,
    val platformStatistics: List<PlatformStatistic>,
    val featuredVenueId: String,
    val serviceIds: List<String>,
    val popularGameIds: List<String>,
)

enum class GameSort(val label: String) {
    RECOMMENDED("پیشنهادی"),
    RATING("بیشترین امتیاز"),
    NEWEST("جدیدترین"),
    TITLE("نام بازی"),
}

data class GameFilter(
    val query: String = "",
    val platform: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val minimumRating: Double? = null,
    val sort: GameSort = GameSort.RECOMMENDED,
)

data class VenueFilter(
    val query: String = "",
    val province: String? = null,
    val city: String? = null,
    val service: String? = null,
    val amenity: String? = null,
)

enum class AuthMode(val label: String) {
    LOGIN("ورود"),
    REGISTER("ثبت‌نام"),
}

data class AuthInput(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
)

data class ValidationResult(val errors: Map<String, String>) {
    val isValid: Boolean get() = errors.isEmpty()
}

data class DemoAuthResult(
    val accepted: Boolean,
    val message: String,
)
