# راهنمای یکپارچه‌سازی API استعداد بازی

این سند قرارداد پیشنهادی بین اپ اندروید و سرور `my.gametalent.ir` برای جایگزینی داده‌های نمونه است. قرارداد باید پیش از پیاده‌سازی سرور و `RemoteGameTalentRepository` تأیید و با تست قرارداد قفل شود. تمام مقادیر این سند نمونه‌اند و هیچ کلید، رمز، توکن یا نشانی محرمانه واقعی در آن وجود ندارد.

## ۱. اصول مشترک قرارداد

### نشانی، نسخه و قالب

- نشانی production: `https://api.my.gametalent.ir/v1`
- نشانی staging نمونه: `https://staging-api.example.invalid/v1`
- فقط HTTPS مجاز است. درخواست HTTP نباید redirect شود و باید در لایه شبکه رد شود.
- درخواست و پاسخ JSON با `Content-Type: application/json; charset=utf-8` هستند.
- زمان‌ها ISO 8601 و UTC هستند، مانند `2026-10-30T08:15:30Z`.
- شناسه‌ها و slugها رشته پایدار هستند. اپ نباید نام نمایشی را به عنوان شناسه ذخیره کند.
- مبلغ با عدد صحیح در کوچک‌ترین واحد پول و کد ارز منتقل می‌شود، نه با متن قالب‌بندی‌شده.
- `null` فقط در فیلدهایی مجاز است که در قرارداد nullable اعلام شده‌اند. فیلد حذف‌شده با مقدار `null` یکسان تلقی نمی‌شود.

هدرهای عمومی درخواست:

```http
Accept: application/json
X-Request-ID: 018f6d7e-86ce-7f45-a7f2-9286fe4f3b61
X-Client-Platform: android
X-Client-Version: 3.0
Accept-Language: fa-IR
```

سرور باید `X-Request-ID` معتبر ورودی را بازتاب دهد یا یک UUID جدید بسازد. این شناسه برای پشتیبانی قابل ارائه است و نباید شامل داده شخصی باشد.

### پاسخ خطای پایدار

تمام خطاهای 4xx و 5xx این envelope را دارند:

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "اطلاعات ارسال‌شده معتبر نیست.",
    "fields": {
      "email": ["فرمت ایمیل معتبر نیست."]
    },
    "request_id": "018f6d7e-86ce-7f45-a7f2-9286fe4f3b61",
    "retryable": false,
    "details": {}
  }
}
```

- `code` مقدار machine-readable و پایدار است. نمونه‌ها: `UNAUTHENTICATED`، `FORBIDDEN`، `NOT_FOUND`، `CONFLICT`، `VALIDATION_FAILED`، `RATE_LIMITED`، `TOKEN_REUSED` و `INTERNAL_ERROR`.
- `message` برای نمایش عمومی است و نباید stack trace، SQL، وجود حساب، سیاست داخلی یا شناسه محرمانه را افشا کند.
- `fields` فقط برای خطای اعتبارسنجی و با کلیدهای قراردادی برگردانده می‌شود.
- 401 برای نبود یا انقضای احراز هویت، 403 برای نداشتن مجوز، 404 برای منبع ناموجود، 409 برای تعارض، 422 برای validation، 429 برای محدودیت نرخ و 5xx برای خطای سرور است.
- اپ بر اساس `code` تصمیم می‌گیرد و متن فارسی سرور را برای منطق برنامه parse نمی‌کند.

### صفحه‌بندی، فیلتر و مرتب‌سازی

فهرست‌های بازی، گیمنت و ارائه‌دهنده خدمت از cursor مات، پایدار و کوتاه‌عمر استفاده می‌کنند:

```json
{
  "items": [],
  "page": {
    "next_cursor": "eyJpZCI6ImZjMjUifQ.sample",
    "has_more": true,
    "limit": 20
  }
}
```

- `limit` پیش‌فرض 20، حداقل 1 و حداکثر 50 است.
- cursor حداکثر 15 دقیقه اعتبار دارد، امضاشده است و کلاینت نباید آن را تفسیر یا تغییر دهد.
- نتیجه با یک tie-breaker پایدار مانند `id` مرتب می‌شود تا تکرار یا حذف ناخواسته رخ ندهد.
- فقط فیلترها و sortهای allowlist شده در این سند پذیرفته می‌شوند. پارامتر ناشناخته یا مقدار خارج از فهرست با 422 رد می‌شود.
- جست‌وجو پس از trim، یکسان‌سازی Unicode فارسی و محدودیت 2 تا 80 نویسه اجرا می‌شود. query خام در log ثبت نمی‌شود.

### نسخه‌بندی و deprecation

نسخه اصلی در path قرار می‌گیرد. تغییر ناسازگار فقط در `/v2` منتشر می‌شود. افزودن فیلد اختیاری در همان نسخه مجاز است و کلاینت باید فیلد ناشناخته را نادیده بگیرد. endpoint منسوخ دست‌کم 6 ماه هم‌زمان فعال می‌ماند و این هدرها را می‌فرستد:

```http
Deprecation: true
Sunset: Fri, 30 Apr 2027 00:00:00 GMT
Link: <https://docs.example.invalid/api/migrations/v2>; rel="deprecation"
```

## ۲. نگاشت صفحه‌ها به endpointها

| صفحه یا تعامل اپ | endpointهای لازم |
| --- | --- |
| خانه، جست‌وجوی hero، آمار پلتفرم، گیمنت منتخب، خدمات و بازی‌های محبوب | `GET /bootstrap`، `GET /home` |
| کاتالوگ بازی و فیلتر، sort و صفحه بعد | `GET /games/filters`، `GET /games` |
| جزئیات، گالری، لینک رسمی، بازی مرتبط و محل نصب | `GET /games/{game_id}`، `GET /games/{game_id}/related`، `GET /games/{game_id}/availability` |
| کاتالوگ گیمنت و فیلترها | `GET /venues/filters`، `GET /locations/provinces`، `GET /locations/cities`، `GET /venues` |
| نقشه موبایل، marker و فیلتر استان | `GET /venues/map` |
| جزئیات گیمنت، تماس و مسیریابی | `GET /venues/{venue_id}` |
| اتاق، دستگاه و وضعیت | `GET /venues/{venue_id}/rooms` |
| بازی‌های یکتا و تعرفه‌ها | `GET /venues/{venue_id}/games`، `GET /venues/{venue_id}/tariffs` |
| فهرست و جزئیات خدمات فنی | `GET /services`، `GET /services/{service_id}` |
| مراکز ارائه‌دهنده خدمت | `GET /services/{service_id}/providers` |
| ورود و ثبت‌نام | `POST /auth/login`، `POST /auth/register` |
| تمدید، خروج و بازیابی نشست | `POST /auth/refresh`، `POST /auth/logout`، `GET /auth/session` |
| حساب کاربری پس از اتصال واقعی | `GET /me`، `PATCH /me` |

`GET /bootstrap` برای شروع اپ است و مقادیر کم‌تغییر را در یک round trip می‌دهد. داده صفحه‌ها همچنان endpoint مستقل دارد تا cache و rollout هر دامنه جدا بماند.

## ۳. Bootstrap، خانه و داده مرجع

### `GET /v1/bootstrap`

پاسخ عمومی و بدون احراز هویت:

```json
{
  "api_version": "v1",
  "server_time": "2026-10-30T08:15:30Z",
  "minimum_android_version_code": 3,
  "mock_fallback_allowed": false,
  "features": {
    "games_api": true,
    "venues_api": true,
    "map_api": true,
    "services_api": true,
    "auth_api": false
  },
  "references": {
    "platforms": [
      {"id": "ps5", "title": "پلی‌استیشن ۵"},
      {"id": "ps4", "title": "پلی‌استیشن ۴"},
      {"id": "pc", "title": "رایانه"}
    ],
    "genres": [
      {"id": "fighting", "title": "مبارزه‌ای"},
      {"id": "sports", "title": "ورزشی"}
    ]
  }
}
```

### `GET /v1/home`

پاسخ عمومی صفحه خانه:

```json
{
  "headline": "بازی و گیمنت مناسب خودت را پیدا کن",
  "subtitle": "بازی‌ها و مراکز نزدیک را بررسی کن",
  "platform_statistics": [
    {"platform_id": "ps5", "title": "پلی‌استیشن ۵", "game_count": 128}
  ],
  "featured_venue": {
    "id": "golbarg",
    "name": "گیمنت گلبرگ",
    "badge": "گیمنت منتخب",
    "province": {"id": "tehran", "title": "تهران"},
    "city": {"id": "rey", "title": "ری"},
    "address_short": "ری، داسار، تمجیدی",
    "room_count": 4,
    "device_count": 16,
    "device_types": ["ps4", "ps5"]
  },
  "services": [
    {
      "id": "install",
      "title": "نصب و به‌روزرسانی بازی",
      "short_description": "نصب قانونی و تنظیم بازی روی کنسول یا رایانه"
    }
  ],
  "popular_games": [
    {
      "id": "fc25",
      "title": "اف‌سی ۲۵",
      "latin_title": "EA Sports FC 25",
      "release_year": 2024,
      "age_rating": "+۳",
      "rating": 4.8,
      "platforms": [{"id": "ps5", "title": "پلی‌استیشن ۵"}],
      "genres": [{"id": "sports", "title": "ورزشی"}],
      "cover_image": {
        "url": "https://cdn.my.gametalent.ir/images/games/fc25.webp",
        "width": 720,
        "height": 960,
        "alt": "تصویر بازی اف‌سی ۲۵"
      }
    }
  ]
}
```

Hero search با submit به کاتالوگ و پارامتر `q` منتقل می‌شود. سرور endpoint جست‌وجوی جدا لازم ندارد.

### استان و شهر

```http
GET /v1/locations/provinces
GET /v1/locations/cities?province_id=tehran
```

```json
{
  "items": [
    {"id": "rey", "province_id": "tehran", "title": "ری"},
    {"id": "tehran-city", "province_id": "tehran", "title": "تهران"}
  ]
}
```

`province_id` در endpoint شهر اجباری است تا فهرست وابسته و معتبر باشد. شناسه استان و شهر از allowlist مرجع پذیرفته می‌شود.

## ۴. بازی‌ها

### فیلترهای بازی

`GET /v1/games/filters` گزینه‌های قابل استفاده در رابط را برمی‌گرداند:

```json
{
  "platforms": [{"id": "ps5", "title": "پلی‌استیشن ۵"}],
  "genres": [{"id": "fighting", "title": "مبارزه‌ای"}],
  "years": [2026, 2025, 2024],
  "minimum_ratings": [4.0, 4.5],
  "sorts": [
    {"id": "recommended", "title": "پیشنهادی"},
    {"id": "rating", "title": "بیشترین امتیاز"},
    {"id": "newest", "title": "جدیدترین"},
    {"id": "title", "title": "نام بازی"}
  ]
}
```

### فهرست بازی

```http
GET /v1/games?q=تکن&platform=ps5&genre=fighting&year=2024&min_rating=4.5&sort=rating&limit=20&cursor={opaque}
```

allowlist پارامترها:

- `platform`: شناسه موجود در `/games/filters`
- `genre`: شناسه موجود در `/games/filters`
- `year`: عدد بین 1970 و سال جاری به اضافه 2
- `min_rating`: عدد 0 تا 5
- `sort`: فقط `recommended`، `rating`، `newest` و `title`

```json
{
  "items": [
    {
      "id": "tekken8",
      "title": "تکن ۸",
      "latin_title": "Tekken 8",
      "release_year": 2024,
      "age_rating": "+۱۶",
      "rating": 4.6,
      "platforms": [{"id": "ps5", "title": "پلی‌استیشن ۵"}],
      "genres": [{"id": "fighting", "title": "مبارزه‌ای"}],
      "cover_image": null,
      "featured": true
    }
  ],
  "page": {"next_cursor": null, "has_more": false, "limit": 20}
}
```

حالت فهرست یا شبکه فقط presentation محلی است و پارامتر API ندارد.

### جزئیات بازی

`GET /v1/games/tekken8`

```json
{
  "id": "tekken8",
  "title": "تکن ۸",
  "latin_title": "Tekken 8",
  "description": "یک بازی مبارزه‌ای سریع با امکان رقابت دونفره.",
  "release_year": 2024,
  "age_rating": "+۱۶",
  "rating": 4.6,
  "rating_count": 241,
  "platforms": [{"id": "ps5", "title": "پلی‌استیشن ۵"}],
  "genres": [{"id": "fighting", "title": "مبارزه‌ای"}],
  "gallery": [
    {
      "id": "tekken8-match",
      "url": "https://cdn.my.gametalent.ir/images/games/tekken8/match.webp",
      "thumbnail_url": "https://cdn.my.gametalent.ir/images/games/tekken8/match-thumb.webp",
      "width": 1280,
      "height": 720,
      "alt": "مسابقه دونفره تکن ۸"
    }
  ],
  "official_url": "https://tekken.com/",
  "updated_at": "2026-10-30T07:00:00Z"
}
```

`official_url` باید در سرور با scheme `https` اعتبارسنجی شود. اپ پیش از باز کردن دوباره scheme و host را بررسی و تأیید خروج از اپ را نمایش می‌دهد.

### بازی مرتبط

`GET /v1/games/tekken8/related?limit=10&cursor={opaque}` همان schema خلاصه بازی و envelope صفحه‌بندی را برمی‌گرداند. sort داخلی فقط بر اساس ارتباط و سپس `id` است و کلاینت sort دلخواه نمی‌فرستد.

### موجودی بازی در گیمنت‌ها

`GET /v1/games/tekken8/availability?province_id=tehran&city_id=rey&limit=20&cursor={opaque}`

```json
{
  "items": [
    {
      "venue": {
        "id": "golbarg",
        "name": "گیمنت گلبرگ",
        "province": {"id": "tehran", "title": "تهران"},
        "city": {"id": "rey", "title": "ری"},
        "address_short": "ری، داسار، تمجیدی"
      },
      "device_types": ["ps4", "ps5"],
      "availability_status": "contact_venue",
      "observed_at": null
    }
  ],
  "page": {"next_cursor": null, "has_more": false, "limit": 20}
}
```

`availability_status` فقط یکی از `available`، `busy`، `offline`، `unknown` و `contact_venue` است. اگر داده لحظه‌ای نیست، سرور نباید وضعیت ساختگی بدهد و باید `contact_venue` یا `unknown` برگرداند.

## ۵. گیمنت‌ها و نقشه

### فیلترهای گیمنت

`GET /v1/venues/filters`

```json
{
  "services": [
    {"id": "game-installation", "title": "نصب بازی"},
    {"id": "controller-repair", "title": "تعمیر دسته"}
  ],
  "amenities": [
    {"id": "cafe", "title": "کافه"},
    {"id": "buffet", "title": "بوفه"}
  ]
}
```

استان و شهر از endpointهای مرجع بخش قبل دریافت می‌شوند.

### فهرست گیمنت

```http
GET /v1/venues?q=گلبرگ&province_id=tehran&city_id=rey&service=game-installation&amenity=cafe&sort=featured&limit=20&cursor={opaque}
```

- `sort` فقط `featured` یا `name` است.
- `province_id`، `city_id`، `service` و `amenity` باید عضو داده مرجع باشند.
- اگر شهر متعلق به استان نباشد، پاسخ 422 است.

```json
{
  "items": [
    {
      "id": "golbarg",
      "name": "گیمنت گلبرگ",
      "badge": "گیمنت منتخب",
      "province": {"id": "tehran", "title": "تهران"},
      "city": {"id": "rey", "title": "ری"},
      "address_short": "ری، داسار، تمجیدی",
      "room_count": 4,
      "device_count": 16,
      "active_device_count": 16,
      "device_types": ["ps4", "ps5"],
      "featured": true,
      "location": {"latitude": 35.5974814, "longitude": 51.4259103},
      "cover_image": null
    }
  ],
  "page": {"next_cursor": null, "has_more": false, "limit": 20}
}
```

### داده نقشه

```http
GET /v1/venues/map?province_id=tehran&north=35.90&east=51.70&south=35.40&west=51.10&zoom=11
```

```json
{
  "viewport": {"north": 35.90, "east": 51.70, "south": 35.40, "west": 51.10},
  "markers": [
    {
      "id": "golbarg",
      "name": "گیمنت گلبرگ",
      "latitude": 35.5974814,
      "longitude": 51.4259103,
      "badge": "گیمنت منتخب",
      "active_device_count": 16
    }
  ],
  "clusters": [],
  "generated_at": "2026-10-30T08:15:00Z"
}
```

مختصات باید در بازه معتبر باشند، مساحت viewport باید سقف داشته باشد و پاسخ حداکثر 500 marker دارد. در zoom پایین سرور cluster برمی‌گرداند. انتخاب لایه ساده یا تیره تنظیم محلی نمایش است. موقعیت فعلی دستگاه فقط پس از رضایت روشن کاربر و مجوز runtime خوانده می‌شود، در log یا analytics ثبت نمی‌شود و برای API فقط در صورت نیاز به مرتب‌سازی نزدیک‌ترین‌ها با دقت کاهش‌یافته فرستاده می‌شود.

### جزئیات گیمنت

`GET /v1/venues/golbarg`

```json
{
  "id": "golbarg",
  "name": "گیمنت گلبرگ",
  "badge": "گیمنت منتخب",
  "province": {"id": "tehran", "title": "تهران"},
  "city": {"id": "rey", "title": "ری"},
  "phone_display": "۰۲۱ ۵۵۹۶ ۱۵۵۹",
  "phone_e164": "+982155961559",
  "address": "ری، داسار، تمجیدی",
  "location": {"latitude": 35.5974814, "longitude": 51.4259103},
  "services": [{"id": "game-installation", "title": "نصب بازی"}],
  "amenities": [{"id": "cafe", "title": "کافه"}],
  "summary": {
    "room_count": 4,
    "device_count": 16,
    "active_device_count": 16
  },
  "status_freshness": {
    "is_live": false,
    "observed_at": "2026-10-30T08:10:00Z"
  },
  "updated_at": "2026-10-30T08:10:00Z"
}
```

اپ برای تماس فقط `phone_e164` و برای مسیریابی مختصات را به Intent سیستم می‌دهد. پاسخ سرور نباید Intent URI دلخواه داشته باشد.

### اتاق‌ها و دستگاه‌ها

`GET /v1/venues/golbarg/rooms`

```json
{
  "items": [
    {
      "id": "vip-ps5",
      "name": "ویژه (PS5)",
      "active_device_count": 3,
      "devices": [
        {
          "id": "device-11",
          "name": "شماره ۱۱",
          "type": {"id": "ps5", "title": "پلی‌استیشن ۵"},
          "active_in_system": true,
          "occupancy": "in_use",
          "observed_at": "2026-10-30T08:10:00Z"
        }
      ]
    }
  ],
  "is_live": true,
  "generated_at": "2026-10-30T08:10:02Z"
}
```

`occupancy` فقط `available`، `in_use`، `offline` یا `unknown` است. داده بدون `observed_at` نباید زنده معرفی شود.

### بازی‌های یکتای مجموعه

`GET /v1/venues/golbarg/games?platform=ps5&limit=20&cursor={opaque}` پاسخ صفحه‌بندی‌شده از خلاصه بازی بخش ۴ را برمی‌گرداند. `platform` از allowlist پلتفرم‌هاست.

### تعرفه‌ها

`GET /v1/venues/golbarg/tariffs`

```json
{
  "items": [
    {
      "id": "golbarg-ps5-hour",
      "device_type": {"id": "ps5", "title": "پلی‌استیشن ۵"},
      "billing_unit": "hour",
      "amount": 1300000,
      "currency": "IRR",
      "display_text": "ساعتی ۱۳۰ هزار تومان",
      "valid_from": "2026-10-01T00:00:00Z",
      "valid_until": null
    }
  ],
  "updated_at": "2026-10-29T13:00:00Z",
  "disclaimer": "قیمت نهایی را با مجموعه تأیید کنید."
}
```

کلاینت محاسبه مالی را از `display_text` انجام نمی‌دهد. تعرفه منقضی در فهرست فعال برگردانده نمی‌شود.

## ۶. خدمات فنی

### فهرست خدمات

`GET /v1/services`

```json
{
  "items": [
    {
      "id": "controller-repair",
      "title": "تعمیر دسته بازی",
      "short_description": "عیب‌یابی کلیدها، آنالوگ و اتصال دسته",
      "provider_count": 2,
      "image": null
    }
  ]
}
```

تعداد این فهرست محدود و بدون cursor است. اگر تعداد از 50 بیشتر شد، همان envelope cursor عمومی اضافه می‌شود.

### جزئیات خدمت

`GET /v1/services/controller-repair`

```json
{
  "id": "controller-repair",
  "title": "تعمیر دسته بازی",
  "short_description": "عیب‌یابی کلیدها، آنالوگ و اتصال دسته",
  "description": "هزینه و زمان تعمیر پس از عیب‌یابی اولیه مشخص می‌شود.",
  "steps": [
    {"order": 1, "title": "ثبت نشانه‌های خرابی"},
    {"order": 2, "title": "عیب‌یابی فنی"},
    {"order": 3, "title": "اعلام هزینه و تعمیر پس از تأیید"}
  ],
  "updated_at": "2026-10-29T13:00:00Z"
}
```

### ارائه‌دهندگان خدمت

`GET /v1/services/controller-repair/providers?province_id=tehran&limit=20&cursor={opaque}` پاسخ صفحه‌بندی‌شده از خلاصه گیمنت بخش ۵ را برمی‌گرداند.

## ۷. احراز هویت، نشست و پروفایل

### مرزهای احراز هویت و authorization

خواندن خانه، بازی‌ها، گیمنت‌ها، نقشه و خدمات عمومی است. endpointهای `/auth/session`، `/auth/logout` و `/me` به access token نیاز دارند. مجوز سمت سرور از مالکیت منبع و scope محاسبه می‌شود. مخفی کردن دکمه در اپ کنترل دسترسی نیست. هر endpoint محافظت‌شده باید authorization را مستقل اجرا کند و از IDOR جلوگیری کند. اپ نقش یا `user_id` ارسالی را مبنای مجوز قرار نمی‌دهد.

access token عمر 10 دقیقه دارد و فقط در هدر زیر ارسال می‌شود:

```http
Authorization: Bearer {access_token}
```

توکن در query string، URL، log یا crash report قرار نمی‌گیرد. refresh token یک مقدار opaque با حداقل 256 بیت entropy، یک‌بارمصرف و دارای عمر مطلق 30 روز و idle timeout هفت روز است. سرور فقط hash توکن را نگه می‌دارد.

### ثبت‌نام

`POST /v1/auth/register`

```http
Idempotency-Key: 018f6d7e-86ce-7f45-a7f2-9286fe4f3b61
```

```json
{
  "name": "کاربر نمونه",
  "email": "user@example.invalid",
  "password": "example-only-password",
  "password_confirmation": "example-only-password",
  "privacy_policy_version": "2026-10",
  "privacy_consent": true
}
```

```json
{
  "user": {
    "id": "usr_01JB6VQ2N4D4",
    "name": "کاربر نمونه",
    "email": "u***@example.invalid",
    "email_verified": false
  },
  "session": {
    "access_token": "ACCESS_TOKEN_PLACEHOLDER",
    "token_type": "Bearer",
    "expires_in": 600,
    "refresh_token": "REFRESH_TOKEN_PLACEHOLDER",
    "refresh_expires_in": 2592000
  }
}
```

`Idempotency-Key` برای 24 ساعت به hash بدنه و پاسخ متصل است. استفاده دوباره با بدنه متفاوت 409 می‌دهد. سرور ایمیل تکراری را در جریان عمومی با متن غیرقابل استفاده برای enumeration مدیریت می‌کند. در صورت نیاز به ورود کاربر موجود، مسیر تأیید مالکیت امن ارائه می‌شود.

### ورود

`POST /v1/auth/login`

```json
{
  "email": "user@example.invalid",
  "password": "example-only-password",
  "device": {
    "installation_id": "random-resettable-installation-id",
    "name": "Android device",
    "app_version": "3.0"
  }
}
```

پاسخ موفق همان `user` و `session` ثبت‌نام است. پاسخ نام کاربری یا رمز اشتباه همیشه کد عمومی `INVALID_CREDENTIALS` دارد تا وجود حساب افشا نشود. `installation_id` تصادفی، قابل reset و مستقل از IMEI، شماره تلفن و advertising ID است.

### refresh چرخشی

`POST /v1/auth/refresh`

```json
{
  "refresh_token": "REFRESH_TOKEN_PLACEHOLDER",
  "installation_id": "random-resettable-installation-id"
}
```

```json
{
  "access_token": "NEW_ACCESS_TOKEN_PLACEHOLDER",
  "token_type": "Bearer",
  "expires_in": 600,
  "refresh_token": "NEW_REFRESH_TOKEN_PLACEHOLDER",
  "refresh_expires_in": 2592000
}
```

هر refresh موفق، refresh token قبلی را مصرف و توکن جدید در همان token family صادر می‌کند. اگر توکن مصرف‌شده دوباره دیده شود، سرور replay را ثبت می‌کند، کل family را revoke می‌کند، پاسخ 401 با `TOKEN_REUSED` می‌دهد و کاربر باید دوباره وارد شود. درخواست‌های refresh در اپ single-flight هستند تا چند درخواست هم‌زمان خود اپ replay کاذب نسازد.

### نشست و خروج

```http
GET /v1/auth/session
POST /v1/auth/logout
Authorization: Bearer {access_token}
```

بدنه خروج:

```json
{
  "refresh_token": "REFRESH_TOKEN_PLACEHOLDER",
  "all_sessions": false
}
```

`GET /auth/session`:

```json
{
  "authenticated": true,
  "user": {
    "id": "usr_01JB6VQ2N4D4",
    "name": "کاربر نمونه",
    "email": "u***@example.invalid",
    "email_verified": false
  },
  "session": {
    "id": "ses_01JB6VR0T13C",
    "created_at": "2026-10-30T08:00:00Z",
    "expires_at": "2026-11-29T08:00:00Z"
  }
}
```

خروج idempotent است و برای نشست از قبل لغوشده نیز 204 می‌دهد. اگر access token منقضی شده باشد، سرور می‌تواند با refresh token معتبر نشست همان family را revoke کند. اپ پس از خروج، ciphertext محلی توکن‌ها را حذف می‌کند.

### پروفایل محافظت‌شده

```http
GET /v1/me
PATCH /v1/me
Authorization: Bearer {access_token}
Idempotency-Key: {uuid}
```

```json
{
  "name": "نام جدید"
}
```

```json
{
  "id": "usr_01JB6VQ2N4D4",
  "name": "نام جدید",
  "email": "u***@example.invalid",
  "email_verified": false,
  "privacy_policy_version": "2026-10",
  "created_at": "2026-10-30T08:00:00Z"
}
```

تغییر ایمیل، رمز، نقش یا وضعیت تأیید endpoint و re-authentication جدا می‌خواهد و در این قرارداد نیست. `PATCH /me` فقط `name` را می‌پذیرد و mass assignment باید رد شود.

## ۸. کنترل‌های امنیتی الزامی

### نگهداری توکن در Android

- access token ترجیحاً فقط در حافظه process نگهداری می‌شود.
- refresh token در فایل یا DataStore رمز‌شده با AES-GCM نگهداری می‌شود. کلید AES باید non-exportable باشد و داخل Android Keystore ساخته شود. خود Keystore محل ذخیره رشته توکن نیست.
- برای دستگاه پشتیبانی‌شده، کلید به hardware-backed Keystore متصل شود. اپ باید نبود سخت‌افزار امن را بدون کاهش پنهانی امنیت مدیریت کند.
- backup برای فایل توکن غیرفعال است و token material نباید وارد Auto Backup شود. پیش از عرضه API باید `dataExtractionRules` و `fullBackupContent` مناسب تنظیم شوند.
- خروج، replay، 401 غیرقابل refresh، پاک شدن داده اپ و invalidation کلید باید توکن محلی را حذف کنند.
- رمز، access token و refresh token هرگز در SavedState، clipboard، analytics، log، screenshot تست یا crash attachment قرار نمی‌گیرند.
- biometric می‌تواند برای باز کردن محلی کلید استفاده شود، ولی جای احراز هویت سرور را نمی‌گیرد.

### TLS و محدودیت cleartext

- حداقل TLS 1.2 و ترجیح TLS 1.3 است. certificate و hostname validation پیش‌فرض پلتفرم نباید bypass شود.
- در manifest نسخه live، `android:usesCleartextTraffic="false"` و Network Security Config بدون cleartext تعریف می‌شود.
- سرور HSTS با `max-age=31536000; includeSubDomains` دارد. فعال‌سازی preload پس از اطمینان از تمام زیردامنه‌ها انجام شود.
- TLS pinning اجباری نیست، چون rotation اشتباه می‌تواند اپ را از کار بیندازد. اگر threat model آن را لازم کرد، حداقل دو SPKI pin، pin پشتیبان و مسیر rotation خارج از انتشار فوری اپ لازم است.
- هیچ trust-all manager، hostname verifier سفارشی ناامن یا certificate خودامضا در production مجاز نیست.

### اعتبارسنجی و normalization

- سرور schema validation، type checking، حد طول، enum allowlist و رد فیلد ناشناخته را در مرز API انجام می‌دهد.
- `name`: پس از trim و Unicode NFC بین 2 تا 80 نویسه.
- `email`: trim، lowercase برای مقایسه دامنه و حداکثر 254 بایت. ایمیل خام فقط برای کارکرد حساب نگهداری می‌شود.
- `password`: بین 8 تا 128 نویسه، بدون trim خودکار، بررسی در برابر فهرست رمزهای افشاشده و hash با Argon2id با پارامتر قابل ارتقا. رمز در log یا analytics ثبت نمی‌شود.
- شناسه، cursor، مختصات، سال، rating و limit با bound مشخص بررسی می‌شوند.
- خروجی متنی به صورت JSON encode می‌شود. وب باید هنگام render کردن HTML escaping مستقل انجام دهد.
- query پایگاه داده parameterized است و نام sort یا column فقط از mapping ثابت سرور انتخاب می‌شود.

### محدودیت نرخ صریح

مقادیر شروع production، قابل تنظیم و قابل بازبینی با telemetry تجمیعی:

| گروه | محدودیت IP | محدودیت حساب یا شناسه |
| --- | --- | --- |
| خواندن عمومی | 120 درخواست در دقیقه | 300 درخواست در دقیقه برای حساب واردشده |
| `POST /auth/login` | 10 تلاش در 15 دقیقه | 5 تلاش در 15 دقیقه برای hash ایمیل نرمال‌شده |
| `POST /auth/register` | 5 تلاش در ساعت | 3 تلاش در 24 ساعت برای hash ایمیل نرمال‌شده |
| `POST /auth/refresh` | 60 درخواست در ساعت | 30 درخواست در ساعت برای token family |
| `/auth/session` و `/me` | 60 درخواست در دقیقه | 30 درخواست در دقیقه برای حساب |
| نقشه | 30 درخواست در دقیقه | 60 درخواست در دقیقه برای حساب |

محدودیت باید با gateway معتبر و IP واقعی پس از trusted proxy اعمال شود. پاسخ 429:

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 42
RateLimit-Limit: 10
RateLimit-Remaining: 0
RateLimit-Reset: 42
```

```json
{
  "error": {
    "code": "RATE_LIMITED",
    "message": "درخواست‌ها بیش از حد مجاز است. کمی بعد تلاش کنید.",
    "fields": {},
    "request_id": "018f6d7e-86ce-7f45-a7f2-9286fe4f3b61",
    "retryable": true,
    "details": {"retry_after_seconds": 42}
  }
}
```

اپ دقیقاً `Retry-After` را رعایت می‌کند و دکمه submit را تا آن زمان محدود می‌کند. محدودیت حساب نباید با تغییر IP دور زده شود. برای جلوگیری از enumeration، شکل پاسخ auth بین حساب موجود و ناموجود تفاوت قابل مشاهده ندارد.

### مقابله با سوءاستفاده

- تأخیر افزایشی، قفل موقت و challenge تطبیقی پس از رفتار مشکوک اعمال شود. CAPTCHA فقط در صورت ریسک و با گزینه دسترس‌پذیر استفاده شود.
- اعتبارسنجی ایمیل و تأیید مالکیت برای ثبت‌نام، جلوگیری از disposable email در صورت نیاز، و محدودیت ساخت حساب اجرا شود.
- credential stuffing با rate limit چندبعدی، تشخیص token replay و اعلان نشست جدید کنترل شود.
- دسترسی مدیران MFA، least privilege، audit و جداسازی endpoint مدیریتی می‌خواهد.
- WAF، محدودیت اندازه body حداکثر 64 KiB برای این endpointها، timeout سرور و سقف هزینه query لازم است.
- هیچ داده‌ای از کلاینت، از جمله نقش، شمارنده، قیمت یا وضعیت دستگاه، بدون مرجع سمت سرور قابل اعتماد نیست.

## ۹. حریم خصوصی، نگهداری و audit

### کمینه‌سازی و رضایت

- endpointهای عمومی به شناسه تبلیغاتی، مخاطبان، شماره تلفن دستگاه یا موقعیت دقیق کاربر نیاز ندارند.
- موقعیت فقط پس از اقدام آگاهانه کاربر، توضیح کاربرد و permission runtime خوانده می‌شود. رد permission نباید کاتالوگ و فیلتر دستی شهر را غیرفعال کند.
- ثبت‌نام نسخه سیاست حریم خصوصی و timestamp رضایت را ذخیره می‌کند. رضایت marketing باید جدا، اختیاری و پیش‌فرض خاموش باشد.
- پاسخ پروفایل فقط فیلد لازم را برمی‌گرداند. ایمیل در نماهای غیرضروری mask می‌شود.
- فرایند دریافت نسخه داده و حذف حساب باید در backend پیش‌بینی شود.

### دوره نگهداری پیشنهادی

- log عملیاتی redacted: حداکثر 30 روز.
- رویداد audit امنیتی شامل login، refresh replay، logout، تغییر پروفایل و عملیات مدیر: 180 روز.
- metadata تلاش ناموفق auth شامل hash یک‌طرفه شناسه و نتیجه: 90 روز.
- شمارنده rate limit: حداکثر 24 ساعت پس از پایان پنجره.
- داده حساب فعال: تا زمان نیاز قراردادی. پس از درخواست حذف، داده عملیاتی حداکثر 30 روزه حذف یا ناشناس شود و backup چرخه حداکثر 90 روزه داشته باشد.
- داده دقیق موقعیت کاربر در سرور ذخیره نمی‌شود، مگر قابلیت آینده با رضایت جدا و retention مستند ایجاد شود.

این بازه‌ها باید با قانون و سیاست نهایی سازمان تأیید شوند. legal hold باید استثنا، دلیل و دسترسی محدود ثبت‌شده داشته باشد.

### logging و audit امن

log ساختاریافته مجاز:

```json
{
  "timestamp": "2026-10-30T08:15:30Z",
  "level": "INFO",
  "event": "auth.login.failed",
  "request_id": "018f6d7e-86ce-7f45-a7f2-9286fe4f3b61",
  "account_ref": "hmac:rotating-pseudonym",
  "ip_prefix": "203.0.113.0/24",
  "reason": "invalid_credentials"
}
```

رمز، token، Authorization header، cookie، ایمیل کامل، شماره تلفن، query جست‌وجو، مختصات دقیق و body auth ممنوع‌اند. query string پیش از log پاک‌سازی می‌شود. audit append-only، زمان‌دار، دارای کنترل دسترسی و هشدار تغییر غیرمجاز است. دسترسی کارکنان به log ثبت و دوره‌ای بازبینی می‌شود.

## ۱۰. cache، idempotency و قابلیت اطمینان

### HTTP caching و ETag

| منبع | سیاست پیشنهادی |
| --- | --- |
| bootstrap و filters | `Cache-Control: public, max-age=300, stale-while-revalidate=86400` |
| خانه، بازی و خدمات | `public, max-age=60, stale-while-revalidate=600` |
| فهرست و جزئیات گیمنت | `public, max-age=30, stale-while-revalidate=300` |
| وضعیت اتاق و دستگاه | `private, max-age=10` یا `no-store` برای داده حساس به زمان |
| auth، session و profile | `Cache-Control: no-store` و `Pragma: no-cache` |

پاسخ GET عمومی ETag قوی یا ضعیف پایدار دارد. اپ در refresh می‌فرستد:

```http
If-None-Match: W/"games-fc25-v7"
```

سرور در صورت نبود تغییر 304 بدون body می‌دهد. cache کلاینت سقف اندازه و پاک‌سازی دارد و پاسخ متعلق به کاربر بین حساب‌ها مشترک نمی‌شود.

### idempotency

`POST /auth/register` و `PATCH /me` با `Idempotency-Key` UUID اجرا می‌شوند. سرور کلید، actor، endpoint، hash بدنه و نتیجه را 24 ساعت نگه می‌دارد. تکرار همان درخواست همان status و پاسخ را برمی‌گرداند. بدنه متفاوت با همان کلید 409 می‌دهد. `POST /auth/logout` ذاتاً idempotent است. login و refresh خودکار retry نمی‌شوند، چون refresh چرخشی نیاز به هماهنگی single-flight دارد.

### timeout و retry کلاینت

- connect timeout: 10 ثانیه.
- read timeout: 20 ثانیه.
- write timeout: 20 ثانیه.
- call timeout: 30 ثانیه.
- GET عمومی روی خطای شبکه، 408، 429 و 502/503/504 حداکثر دو بار با exponential backoff و jitter، ابتدا حدود 500 ms و سپس 1500 ms retry می‌شود.
- `Retry-After` بر backoff محلی اولویت دارد.
- 4xx به جز 408 و 429 retry نمی‌شود.
- عملیات auth و mutation فقط با idempotency معتبر retry می‌شوند. refresh به صورت single-flight و حداکثر یک بار انجام می‌شود.
- در offline، داده cache شده با برچسب زمان آخرین به‌روزرسانی نمایش داده می‌شود. نبود cache باید حالت خطای قابل retry نشان دهد، نه داده ساختگی بدون برچسب.

### اعتماد به URL تصویر و لینک خارجی

- تصویر فقط از `https://cdn.my.gametalent.ir` یا hostهای CDN صریح build-time بارگیری می‌شود.
- redirect به host خارج از allowlist رد می‌شود. schemeهای `file`، `content`، `javascript`، host محلی، loopback و IP خصوصی رد می‌شوند تا SSRF و دسترسی محلی ممکن نشود.
- SVG فعال یا محتوای اجرایی پذیرفته نمی‌شود. MIME، magic bytes، ابعاد و حداکثر حجم 5 MiB بررسی می‌شود.
- CDN باید محتوای upload شده را re-encode، metadata را حذف و نام فایل را مستقل از ورودی کاربر بسازد.
- لینک رسمی بازی فقط HTTPS است و اپ host را با allowlist ثابت build-time بررسی می‌کند. allowlist نسخه mock شامل `ea.com`، `tekken.com`، `mortalkombat.com`، `gran-turismo.com`، `playvalorant.com` و `my.gametalent.ir` و زیردامنه‌های آن‌هاست. افزودن میزبان جدید به بازبینی و انتشار اپ نیاز دارد؛ پاسخ API اجازه گسترش allowlist را ندارد.
- لینک معتبر فقط پس از نمایش نام host و تأیید روشن خروج از اپ در مرورگر سیستم باز می‌شود. لینک دارای user-info، fragment، port غیر از 443، host مشابه یا scheme دیگر رد می‌شود. Intentهای تماس و نقشه مسیرهای typed جدا دارند و از policy لینک رسمی عبور نمی‌کنند.

### CORS

CORS مکانیزم مرورگر است و از API در برابر اپ native Android، curl یا مهاجم server-side محافظت نمی‌کند. امنیت اپ native به TLS، token، authorization، validation و rate limit وابسته است. با این حال frontend وب همچنان باید allowlist دقیق origin مانند `https://my.gametalent.ir`، method و header لازم را داشته باشد. `Access-Control-Allow-Origin: *` همراه credential ممنوع است. درخواست native نباید برای کار کردن Origin جعلی ارسال کند.

## ۱۱. مهاجرت مرحله‌ای از mock به API

### مرحله ۱: تأیید قرارداد

1. backend، Android، وب، امنیت و محصول endpointها، enumها، nullableها، retention و freshness را تأیید کنند.
2. OpenAPI 3.1 از این قرارداد ساخته و در همان repository نسخه‌بندی شود.
3. fixtureهای staging بدون داده واقعی برای گلبرگ، بازی‌ها، خدمات، خطا، صفحه خالی و cursor آماده شوند.
4. consumer contract test اپ و provider contract test backend در CI، schema و error envelope را بررسی کنند.

### مرحله ۲: لایه شبکه امن

1. serializer با رد type نامعتبر و تحمل فیلد ناشناخته اضافه شود.
2. client HTTPS با timeout، cache، ETag، request ID، redacted logging و بدون body logging برای auth ساخته شود.
3. رابط‌های همگام فعلی فقط برای `InMemoryMockRepository` هستند. پیش از شبکه، یک قرارداد ناهمگام جدید برای هر دامنه تعریف شود که stream یا تابع `suspend` برگرداند و نتیجه آن دست‌کم حالت‌های `loading`، `data`، `empty`، `stale` و `error` با `retryable` و `request_id` را نمایش دهد. فهرست‌های راه‌دور باید `items`، `next_cursor`، `has_more` و freshness را در مدل صفحه نگه دارند.
4. صفحه‌ها در همان تغییر قرارداد به مصرف state ناهمگام، نمایش loading/error/retry، حفظ داده stale و درخواست cursor بعدی مهاجرت کنند. در نتیجه `RemoteGameTalentRepository` جایگزین drop-in رابط‌های فعلی نیست و rollout شبکه پیش از این تغییر هماهنگ UI و repository مجاز نیست.
5. DTO شبکه از مدل UI جدا و mapping آن unit test شود.
6. INTERNET permission، منع cleartext، backup exclusion توکن و Keystore storage فقط هم‌زمان با فعال شدن remote اضافه شوند.

### مرحله ۳: rollout خواندنی به تفکیک دامنه

دامنه‌ها جداگانه و به این ترتیب فعال شوند: bootstrap و references، خانه، بازی‌ها، گیمنت‌ها و نقشه، سپس خدمات. انتخاب repository با BuildConfig برای محیط و feature flag امضاشده سمت سرور انجام شود. production build هرگز به staging یا mock پنهان fallback نکند. rollout ابتدا برای تیم داخلی، سپس 5 درصد، 25 درصد و 100 درصد کاربران انجام شود.

معیار توقف شامل افزایش crash، خطای schema، latency p95 بیشتر از 2 ثانیه، نرخ 5xx بیشتر از 1 درصد یا اختلاف نتیجه contract است. rollback باید فقط flag را تغییر دهد و نیازمند انتشار APK فوری نباشد.

### مرحله ۴: cache و fallback

- cache آخرین پاسخ موفق عمومی با timestamp نگهداری شود.
- در قطعی شبکه، داده cache شده با برچسب «آخرین به‌روزرسانی» نمایش داده شود.
- mock فقط در flavor توسعه و تست باقی بماند. production نباید داده mock را به عنوان live نشان دهد.
- وضعیت دستگاه و تعرفه قدیمی باید هشدار freshness واضح داشته باشد.

### مرحله ۵: auth

1. ثبت‌نام و ورود ابتدا در staging با حساب‌های تست و refresh rotation آزمایش شوند.
2. تست replay، revoke family، logout، انقضا، ساعت نادرست دستگاه، reinstall و invalidation Keystore اجرا شود.
3. auth با feature flag جدا و rollout محدود فعال شود.
4. observability فقط event، latency، status، request ID و pseudonymous account reference را ثبت کند. ایمیل، رمز و token ممنوع است.

### مرحله ۶: حذف mock از production

پس از دو نسخه پایدار و تأیید metrics، fallback mock از production حذف می‌شود. fixture و `InMemoryMockRepository` برای preview، unit test و build آفلاین باقی می‌مانند. مستندات، OpenAPI و checklist release باید با هر تغییر قرارداد به‌روز شوند.

## ۱۲. تنظیم محیط بدون secret واقعی

نمونه placeholder امن:

```properties
API_BASE_URL=https://staging-api.example.invalid/v1
API_ENVIRONMENT=staging
API_PUBLIC_CLIENT_ID=ANDROID_PUBLIC_CLIENT_ID_PLACEHOLDER
CERT_PIN_PRIMARY=sha256/PRIMARY_SPKI_PLACEHOLDER
CERT_PIN_BACKUP=sha256/BACKUP_SPKI_PLACEHOLDER
```

- secret کلاینت، رمز signing، کلید خصوصی، database URL دارای credential و service token نباید داخل APK، BuildConfig یا repository قرار گیرد.
- Android یک public client است و secret قابل حفاظت ندارد. OAuth یا جریان مشابه باید از PKCE استفاده کند.
- secretهای backend فقط از secret manager و identity کوتاه‌عمر workload خوانده می‌شوند و دوره rotation دارند.
- فایل local environment در gitignore است. CI secret را mask می‌کند و secret scanning قبل از merge اجرا می‌شود.

## ۱۳. چک‌لیست تحویل backend

- [ ] OpenAPI 3.1 و fixtureهای نمونه با این قرارداد منطبق‌اند.
- [ ] تمام endpointها فقط HTTPS هستند و cleartext رد می‌شود.
- [ ] auth از access token ده دقیقه‌ای و refresh token چرخشی با replay detection و revoke family استفاده می‌کند.
- [ ] authorization و جلوگیری از IDOR برای هر endpoint محافظت‌شده تست شده است.
- [ ] validation، normalization، enum و sort allowlist و query parameterization تست شده‌اند.
- [ ] rate limitهای IP و حساب اعمال و `Retry-After` بررسی شده است.
- [ ] error envelope، request ID، status code و پیام غیرحساس در تست قرارداد پوشش دارند.
- [ ] cursor پایدار، limit حداکثر 50 و داده خالی یا انتهای صفحه تست شده است.
- [ ] ETag، 304، Cache-Control و منع cache پاسخ auth تأیید شده‌اند.
- [ ] idempotency ثبت‌نام و profile با تکرار و body متفاوت تست شده است.
- [ ] image host allowlist، redirect، MIME، اندازه و re-encode بررسی شده‌اند.
- [ ] log و audit هیچ رمز، token، ایمیل کامل، مختصات دقیق یا body حساس ندارند.
- [ ] retention، حذف حساب، backup expiry و دسترسی کارکنان تأیید شده‌اند.
- [ ] CORS فقط originهای وب مجاز را می‌پذیرد و به عنوان کنترل امنیت native استفاده نشده است.
- [ ] dashboard برای latency، 4xx/5xx، rate limit، refresh replay و schema failure بدون داده شخصی آماده است.
- [ ] rollback feature flag و staged rollout در staging آزمایش شده است.
- [ ] تست امنیتی dependency، SAST، secret scan و تست نفوذ endpointهای auth انجام شده است.
