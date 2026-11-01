package com.gamenetmehdi.app

interface HomeRepository {
    fun home(): HomeContent
}

interface GamesRepository {
    fun games(filter: GameFilter = GameFilter()): List<Game>
    fun game(id: String): Game?
    fun platforms(): List<String>
    fun genres(): List<String>
    fun years(): List<Int>
}

interface VenuesRepository {
    fun venues(filter: VenueFilter = VenueFilter()): List<Venue>
    fun venue(id: String): Venue?
    fun provinces(): List<String>
    fun cities(province: String): List<String>
    fun venueServices(): List<String>
    fun amenities(): List<String>
}

interface ServicesRepository {
    fun services(): List<TechnicalService>
    fun service(id: String): TechnicalService?
}

interface AuthRepository {
    fun validate(mode: AuthMode, input: AuthInput): ValidationResult
    fun submitDemo(mode: AuthMode, input: AuthInput): DemoAuthResult
}

interface AppRepository : HomeRepository, GamesRepository, VenuesRepository, ServicesRepository, AuthRepository

object AuthValidator {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(mode: AuthMode, input: AuthInput): ValidationResult {
        val errors = linkedMapOf<String, String>()
        if (mode == AuthMode.REGISTER && input.name.trim().length < 2) {
            errors["name"] = "نام باید حداقل ۲ نویسه باشد"
        }
        if (!emailPattern.matches(input.email.trim())) {
            errors["email"] = "ایمیل معتبر وارد کنید"
        }
        if (input.password.length < 8) {
            errors["password"] = "رمز عبور باید حداقل ۸ نویسه باشد"
        }
        if (mode == AuthMode.REGISTER && input.password != input.passwordConfirmation) {
            errors["passwordConfirmation"] = "تکرار رمز عبور یکسان نیست"
        }
        return ValidationResult(errors)
    }
}

class InMemoryMockRepository : AppRepository {
    private val allGames = mockGames
    private val allVenues = mockVenues
    private val allServices = mockServices

    override fun home(): HomeContent = mockHome

    override fun games(filter: GameFilter): List<Game> {
        val query = filter.query.searchable()
        val filtered = allGames.filter { game ->
            (query.isBlank() || listOf(game.title, game.latinTitle).any { query in it.searchable() }) &&
                (filter.platform == null || filter.platform in game.platforms) &&
                (filter.genre == null || filter.genre in game.genres) &&
                (filter.year == null || filter.year == game.releaseYear) &&
                (filter.minimumRating == null || game.rating >= filter.minimumRating)
        }
        return when (filter.sort) {
            GameSort.RECOMMENDED -> filtered.sortedWith(compareByDescending<Game> { it.featured }.thenByDescending { it.rating })
            GameSort.RATING -> filtered.sortedByDescending { it.rating }
            GameSort.NEWEST -> filtered.sortedByDescending { it.releaseYear }
            GameSort.TITLE -> filtered.sortedBy { it.title }
        }
    }

    override fun game(id: String): Game? = allGames.find { it.id == id }
    override fun platforms(): List<String> = allGames.flatMap { it.platforms }.distinct().sorted()
    override fun genres(): List<String> = allGames.flatMap { it.genres }.distinct().sorted()
    override fun years(): List<Int> = allGames.map { it.releaseYear }.distinct().sortedDescending()

    override fun venues(filter: VenueFilter): List<Venue> {
        val query = filter.query.searchable()
        return allVenues.filter { venue ->
            (query.isBlank() || listOf(venue.name, venue.address, venue.city).any { query in it.searchable() }) &&
                (filter.province == null || filter.province == venue.province) &&
                (filter.city == null || filter.city == venue.city) &&
                (filter.service == null || filter.service in venue.services) &&
                (filter.amenity == null || filter.amenity in venue.amenities)
        }.sortedWith(compareByDescending<Venue> { it.featured }.thenBy { it.name })
    }

    override fun venue(id: String): Venue? = allVenues.find { it.id == id }
    override fun provinces(): List<String> = allVenues.map { it.province }.distinct().sorted()
    override fun cities(province: String): List<String> = allVenues
        .filter { it.province == province }
        .map { it.city }
        .distinct()
        .sorted()
    override fun venueServices(): List<String> = allVenues.flatMap { it.services }.distinct().sorted()
    override fun amenities(): List<String> = allVenues.flatMap { it.amenities }.distinct().sorted()

    override fun services(): List<TechnicalService> = allServices
    override fun service(id: String): TechnicalService? = allServices.find { it.id == id }

    override fun validate(mode: AuthMode, input: AuthInput): ValidationResult = AuthValidator.validate(mode, input)

    override fun submitDemo(mode: AuthMode, input: AuthInput): DemoAuthResult {
        val validation = validate(mode, input)
        return if (validation.isValid) {
            DemoAuthResult(
                accepted = true,
                message = "اطلاعات معتبر است؛ در حالت نمونه هیچ حساب یا نشست واقعی ایجاد نشد.",
            )
        } else {
            DemoAuthResult(false, "لطفاً خطاهای فرم را برطرف کنید.")
        }
    }
}

private fun String.searchable(): String = trim().lowercase().replace('ي', 'ی').replace('ك', 'ک')

private const val PS4 = "پلی‌استیشن ۴"
private const val PS5 = "پلی‌استیشن ۵"
private const val PC = "رایانه"

private val golbargRooms = listOf(
    Room("کودکان", (1..5).map { Device("شماره $it", PS4) }),
    Room("بزرگسالان", listOf(10, 6, 7, 8, 9).map { Device("شماره $it", PS4) }),
    Room(
        "ویژه (PS5)",
        listOf(
            Device("شماره ۱۱", PS5, inUse = true),
            Device("شماره ۱۲", PS5),
            Device("شماره ۱۳", PS5),
        ),
    ),
    Room("قهوه‌خانه", (1..3).map { Device("تخت شماره $it", "تخت") }),
)

val mockVenues = listOf(
    Venue(
        id = "golbarg",
        name = "گیمنت گلبرگ",
        badge = "گیمنت منتخب",
        province = "تهران",
        city = "ری",
        phone = "02155961559",
        address = "ری، داسار، تمجیدی",
        latitude = 35.5974814,
        longitude = 51.4259103,
        services = listOf("نصب بازی", "تعمیر دستگاه", "تعمیر دسته", "فروش دستگاه نو", "فروش دستگاه کارکرده"),
        amenities = listOf("بوفه", "کافه", "سرویس بهداشتی ایرانی"),
        rooms = golbargRooms,
        uniqueGames = listOf("EA Sports FC 25", "Tekken 8", "Mortal Kombat 1", "God of War Ragnarök"),
        tariffs = listOf(
            Tariff(PS4, "ساعتی ۸۰ هزار تومان"),
            Tariff(PS5, "ساعتی ۱۳۰ هزار تومان"),
            Tariff("تخت", "ساعتی ۶۰ هزار تومان"),
        ),
        featured = true,
    ),
    Venue(
        id = "pixel",
        name = "گیم‌نت پیکسل",
        badge = "گیم‌نت",
        province = "تهران",
        city = "تهران",
        phone = "02100000000",
        address = "تهران، میدان هفت تیر",
        latitude = 35.716,
        longitude = 51.425,
        services = listOf("نصب بازی", "مسابقات گروهی"),
        amenities = listOf("بوفه", "اینترنت پرسرعت"),
        rooms = listOf(Room("سالن رایانه", (1..8).map { Device("سیستم $it", PC, inUse = it < 3) })),
        uniqueGames = listOf("Valorant", "Counter-Strike 2"),
        tariffs = listOf(Tariff(PC, "ساعتی ۹۵ هزار تومان")),
    ),
    Venue(
        id = "shiraz-arena",
        name = "شیراز آرنا",
        badge = "باشگاه بازی",
        province = "فارس",
        city = "شیراز",
        phone = "07100000000",
        address = "شیراز، بلوار چمران",
        latitude = 29.62,
        longitude = 52.50,
        services = listOf("تعمیر دسته", "مسابقات گروهی"),
        amenities = listOf("پارکینگ", "کافه"),
        rooms = listOf(Room("کنسول", listOf(Device("کنسول ۱", PS5), Device("کنسول ۲", PS5)))),
        uniqueGames = listOf("Gran Turismo 7", "Tekken 8"),
        tariffs = listOf(Tariff(PS5, "ساعتی ۱۴۰ هزار تومان")),
    ),
)

val mockGames = listOf(
    Game(
        id = "fc25",
        title = "اف‌سی ۲۵",
        latinTitle = "EA Sports FC 25",
        description = "شبیه‌ساز فوتبال با حالت‌های رقابتی و گروهی برای علاقه‌مندان مسابقات باشگاهی.",
        releaseYear = 2024,
        ageRating = "+۳",
        rating = 4.8,
        platforms = listOf(PS4, PS5, "ایکس‌باکس"),
        genres = listOf("ورزشی", "رقابتی"),
        galleryLabels = listOf("مسابقه باشگاهی", "حالت گروهی", "نمای ورزشگاه"),
        officialUrl = "https://www.ea.com/games/ea-sports-fc/fc-25",
        relatedGameIds = listOf("tekken8", "gt7"),
        venueIds = listOf("golbarg"),
        featured = true,
    ),
    Game(
        id = "tekken8",
        title = "تکن ۸",
        latinTitle = "Tekken 8",
        description = "یک بازی مبارزه‌ای سریع با شخصیت‌های متنوع و امکان رقابت دونفره.",
        releaseYear = 2024,
        ageRating = "+۱۶",
        rating = 4.6,
        platforms = listOf(PS5, PC, "ایکس‌باکس"),
        genres = listOf("مبارزه‌ای", "رقابتی"),
        galleryLabels = listOf("مبارزه دونفره", "شخصیت‌ها", "محیط مسابقه"),
        officialUrl = "https://tekken.com/",
        relatedGameIds = listOf("fc25", "mk1"),
        venueIds = listOf("golbarg", "shiraz-arena"),
        featured = true,
    ),
    Game(
        id = "mk1",
        title = "مورتال کامبت ۱",
        latinTitle = "Mortal Kombat 1",
        description = "بازآفرینی مجموعه مبارزه‌ای مورتال کامبت با مبارزات سینمایی.",
        releaseYear = 2023,
        ageRating = "+۱۸",
        rating = 4.2,
        platforms = listOf(PS5, PC, "ایکس‌باکس"),
        genres = listOf("مبارزه‌ای"),
        galleryLabels = listOf("مبارزان", "حالت داستانی"),
        officialUrl = "https://www.mortalkombat.com/",
        relatedGameIds = listOf("tekken8"),
        venueIds = listOf("golbarg"),
    ),
    Game(
        id = "gt7",
        title = "گرن توریسمو ۷",
        latinTitle = "Gran Turismo 7",
        description = "تجربه رانندگی و مسابقه با خودروهای متنوع و مسیرهای دقیق.",
        releaseYear = 2022,
        ageRating = "+۳",
        rating = 4.5,
        platforms = listOf(PS4, PS5),
        genres = listOf("رانندگی", "ورزشی"),
        galleryLabels = listOf("پیست مسابقه", "گاراژ خودرو"),
        officialUrl = "https://www.gran-turismo.com/",
        relatedGameIds = listOf("fc25"),
        venueIds = listOf("shiraz-arena"),
    ),
    Game(
        id = "valorant",
        title = "ولورانت",
        latinTitle = "Valorant",
        description = "بازی تیراندازی تیمی و تاکتیکی با مسابقات کوتاه و رقابتی.",
        releaseYear = 2020,
        ageRating = "+۱۶",
        rating = 4.4,
        platforms = listOf(PC),
        genres = listOf("تیراندازی", "رقابتی"),
        galleryLabels = listOf("نقشه تیمی", "قهرمان‌ها"),
        officialUrl = "https://playvalorant.com/",
        relatedGameIds = emptyList(),
        venueIds = listOf("pixel"),
    ),
)

val mockServices = listOf(
    TechnicalService(
        id = "install",
        title = "نصب و به‌روزرسانی بازی",
        shortDescription = "نصب قانونی و تنظیم بازی روی کنسول یا رایانه",
        description = "کارشناس مجموعه فضای دستگاه و سازگاری نسخه را بررسی می‌کند و زمان تقریبی را پیش از شروع اعلام می‌کند.",
        steps = listOf("بررسی دستگاه و فضای خالی", "انتخاب نسخه سازگار", "نصب، به‌روزرسانی و آزمون اجرا"),
        venueIds = listOf("golbarg", "pixel"),
    ),
    TechnicalService(
        id = "controller-repair",
        title = "تعمیر دسته بازی",
        shortDescription = "عیب‌یابی کلیدها، آنالوگ و اتصال دسته",
        description = "هزینه و زمان تعمیر پس از عیب‌یابی اولیه و تأیید شما مشخص می‌شود.",
        steps = listOf("ثبت نشانه‌های خرابی", "عیب‌یابی فنی", "اعلام هزینه نمونه و تعمیر پس از تأیید"),
        venueIds = listOf("golbarg", "shiraz-arena"),
    ),
    TechnicalService(
        id = "console-repair",
        title = "سرویس و تعمیر کنسول",
        shortDescription = "بررسی نرم‌افزار، تهویه و قطعات دستگاه",
        description = "اطلاعات این صفحه نمونه است و برای تشخیص قطعی باید دستگاه حضوری بررسی شود.",
        steps = listOf("پذیرش و ثبت وضعیت", "آزمون سخت‌افزار و نرم‌افزار", "اعلام نتیجه و زمان تحویل"),
        venueIds = listOf("golbarg"),
    ),
)

val mockHome = HomeContent(
    headline = "بازی و گیمنت مناسب خودت را پیدا کن",
    subtitle = "نسخه نمونه آفلاین از صفحات اصلی استعداد بازی",
    platformStatistics = listOf(
        PlatformStatistic(PS5, 128),
        PlatformStatistic(PS4, 214),
        PlatformStatistic(PC, 96),
    ),
    featuredVenueId = "golbarg",
    serviceIds = mockServices.map { it.id },
    popularGameIds = mockGames.filter { it.featured }.map { it.id },
)
