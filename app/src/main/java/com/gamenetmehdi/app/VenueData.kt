package com.gamenetmehdi.app

data class Device(
    val name: String,
    val type: String,
    val activeInSystem: Boolean = true,
    val inUse: Boolean = false,
)

data class Room(
    val name: String,
    val exclusiveGames: Int,
    val devices: List<Device>,
) {
    val activeDeviceCount: Int get() = devices.count { it.activeInSystem }
}

data class Venue(
    val name: String,
    val badge: String,
    val location: String,
    val phone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val services: List<String>,
    val amenities: List<String>,
    val rooms: List<Room>,
) {
    val roomCount: Int get() = rooms.size
    val deviceCount: Int get() = rooms.sumOf { it.devices.size }
    val activeDeviceCount: Int get() = rooms.sumOf { it.activeDeviceCount }
}

private const val PS4 = "پلی\u200cاستیشن ۴"
private const val PS5 = "پلی\u200cاستیشن ۵"
private const val BED = "تخت"

val golbargVenue = Venue(
    name = "گیمنت گلبرگ",
    badge = "گیمنت",
    location = "تهران، ری",
    phone = "02155961559",
    address = "ری، داسار، تمجیدی",
    latitude = 35.5974814,
    longitude = 51.4259103,
    services = listOf(
        "نصب بازی",
        "تعمیر دستگاه",
        "تعمیر دسته",
        "فروش دستگاه نو",
        "فروش دستگاه کارکرده",
    ),
    amenities = listOf(
        "بوفه",
        "کافه",
        "سرویس بهداشتی ایرانی",
    ),
    rooms = listOf(
        Room(
            name = "کودکان",
            exclusiveGames = 37,
            devices = (1..5).map { Device("شماره $it", PS4) },
        ),
        Room(
            name = "بزرگسالان",
            exclusiveGames = 43,
            devices = listOf(10, 6, 7, 8, 9).map { Device("شماره $it", PS4) },
        ),
        Room(
            name = "ویژه ( PS5 )",
            exclusiveGames = 20,
            devices = listOf(
                Device("شماره 11", PS5, inUse = true),
                Device("شماره 12", PS5),
                Device("شماره 13", PS5),
            ),
        ),
        Room(
            name = "قهوه خانه",
            exclusiveGames = 0,
            devices = (1..3).map { Device("تخت شماره $it", BED) },
        ),
    ),
)
