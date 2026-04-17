# holidays.rest Kotlin SDK

Official Kotlin SDK for the [holidays.rest](https://holidays.rest) API.

## Requirements

- Kotlin 2.0+ / JVM 17+
- Dependencies: `kotlinx-coroutines-core`, `kotlinx-serialization-json` (both JetBrains official)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("rest.holidays:holidays-rest:1.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>rest.holidays</groupId>
    <artifactId>holidays-rest</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```kotlin
import kotlinx.coroutines.runBlocking
import rest.holidays.HolidaysClient
import rest.holidays.HolidaysParams

fun main() = runBlocking {
    HolidaysClient(apiKey = "YOUR_API_KEY").use { client ->
        val holidays = client.getHolidays(HolidaysParams(country = "US", year = 2024))
        holidays.forEach { println("${it.date} — ${it.name["en"]}") }
    }
}
```

Get an API key at [holidays.rest/dashboard](https://www.holidays.rest/dashboard).

---

## API

### `HolidaysClient`

```kotlin
val client = HolidaysClient(
    apiKey  = "YOUR_API_KEY",            // required
    baseUrl = "https://...",             // optional, override for testing
    timeout = Duration.ofSeconds(15),    // optional, default 15s
)
```

Implements `AutoCloseable` — use with Kotlin's `use {}` block for safe cleanup.

---

### `getHolidays(HolidaysParams)` → `List<Holiday>`

```kotlin
data class HolidaysParams(
    val country:  String,           // required — ISO 3166 alpha-2 (e.g. "US")
    val year:     Int,              // required — e.g. 2024

    val month:    Int?     = null,  // optional — 1–12
    val day:      Int?     = null,  // optional — 1–31
    val type:     List<String>? = null,  // "religious", "national", "local"
    val religion: List<Int>?    = null,  // religion codes 1–11
    val region:   List<String>? = null,  // subdivision codes from getCountry()
    val lang:     List<String>? = null,  // language codes from getLanguages()
    val response: String?  = null,  // "json" | "xml" | "yaml" | "csv"
)
```

```kotlin
// All US holidays in 2024
client.getHolidays(HolidaysParams(country = "US", year = 2024))

// National holidays only
client.getHolidays(HolidaysParams(country = "DE", year = 2024, type = listOf("national")))

// Multiple types
client.getHolidays(HolidaysParams(country = "TR", year = 2024, type = listOf("national", "religious")))

// Filter by month and day
client.getHolidays(HolidaysParams(country = "GB", year = 2024, month = 12, day = 25))

// Specific region
client.getHolidays(HolidaysParams(country = "US", year = 2024, region = listOf("US-CA")))

// Multiple regions
client.getHolidays(HolidaysParams(country = "US", year = 2024, region = listOf("US-CA", "US-NY")))
```

---

### `getCountries()` → `List<Country>`

```kotlin
val countries = client.getCountries()
countries.forEach { println("${it.alpha2} — ${it.name}") }
```

---

### `getCountry(countryCode)` → `Country`

Returns country details including subdivision codes usable as `region` filters.

```kotlin
val us = client.getCountry("US")
us.subdivisions.forEach { println("${it.code} — ${it.name}") }
```

---

### `getLanguages()` → `List<Language>`

```kotlin
val languages = client.getLanguages()
```

---

## Models

All responses deserialize into Kotlin `data class` objects.

```kotlin
data class HolidayDay(
    val actual: String,    // day name, e.g. "Thursday"
    val observed: String,  // observed day name (may differ for substitute holidays)
)

data class Holiday(
    val countryCode: String,        // ISO 3166 alpha-2, e.g. "DE"
    val countryName: String,        // e.g. "Germany"
    val date: String,               // ISO 8601, e.g. "2026-01-01"
    val name: Map<String, String>,  // language code → name, e.g. name["en"] = "New Year's Day"
    val isNational: Boolean,
    val isReligious: Boolean,
    val isLocal: Boolean,
    val isEstimate: Boolean,
    val day: HolidayDay,
    val religion: String,           // e.g. "Christianity" or ""
    val regions: List<String>,      // subdivision codes, e.g. ["BW", "BY"]
)

data class Country(
    val name: String, val alpha2: String, val subdivisions: List<Subdivision>
)

data class Subdivision(val code: String, val name: String)

data class Language(val code: String, val name: String)
```

---

## Error Handling

Non-2xx responses throw `HolidaysApiException`:

```kotlin
import rest.holidays.HolidaysApiException

try {
    val holidays = client.getHolidays(HolidaysParams(country = "US", year = 2024))
} catch (e: HolidaysApiException) {
    println(e.statusCode)  // HTTP status code (Int)
    println(e.message)     // Error message (String)
    println(e.body)        // Raw response body (String)
}
```

| Status | Meaning             |
|--------|---------------------|
| 400    | Bad request         |
| 401    | Invalid API key     |
| 404    | Not found           |
| 500    | Server error        |
| 503    | Service unavailable |

---

## Publishing

```bash
./gradlew publishToMavenLocal          # local testing
./gradlew publish                      # publish to configured repository
```

---

## License

MIT
