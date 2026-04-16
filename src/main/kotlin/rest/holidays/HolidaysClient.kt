package rest.holidays

import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private const val DEFAULT_BASE_URL = "https://api.holidays.rest/v1"

/**
 * Async client for the holidays.rest API.
 *
 * All methods are `suspend` functions and must be called from a coroutine scope.
 *
 * Example:
 * ```kotlin
 * val client = HolidaysClient(apiKey = "YOUR_API_KEY")
 * val holidays = client.getHolidays(HolidaysParams(country = "US", year = 2024))
 * client.close()
 * ```
 *
 * Or with [use] for automatic cleanup:
 * ```kotlin
 * HolidaysClient(apiKey = "YOUR_API_KEY").use { client ->
 *     val holidays = client.getHolidays(HolidaysParams(country = "US", year = 2024))
 * }
 * ```
 *
 * @param apiKey Bearer token from https://www.holidays.rest/dashboard.
 * @param baseUrl Override base URL. Useful for testing.
 * @param timeout HTTP request timeout. Default 15 seconds.
 * @param httpClient Provide a custom [HttpClient]. If omitted, one is created internally.
 */
class HolidaysClient(
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    timeout: Duration = Duration.ofSeconds(15),
    httpClient: HttpClient? = null,
) : AutoCloseable {

    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank" }
    }

    private val http: HttpClient = httpClient ?: HttpClient.newBuilder()
        .connectTimeout(timeout)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private val base = baseUrl.trimEnd('/')

    // ── public API ────────────────────────────────────────────────────────

    /**
     * Fetches public holidays matching the given [params].
     */
    suspend fun getHolidays(params: HolidaysParams): List<Holiday> {
        require(params.country.isNotBlank()) { "HolidaysParams.country must not be blank" }

        val query = buildQuery(
            "country" to params.country,
            "year"    to params.year.toString(),
            "month"   to params.month?.toString(),
            "day"     to params.day?.toString(),
            "type"    to params.type?.joinToString(","),
            "religion" to params.religion?.joinToString(","),
            "region"  to params.region?.joinToString(","),
            "lang"    to params.lang?.joinToString(","),
            "response" to params.response,
        )
        return get("$base/holidays$query")
    }

    /** Returns all supported countries. */
    suspend fun getCountries(): List<Country> = get("$base/countries")

    /**
     * Returns details for one country, including subdivision codes
     * usable as [HolidaysParams.region] filters.
     *
     * @param countryCode ISO 3166 alpha-2 code, e.g. `"US"`.
     */
    suspend fun getCountry(countryCode: String): Country {
        require(countryCode.isNotBlank()) { "countryCode must not be blank" }
        return get("$base/country/${encode(countryCode)}")
    }

    /** Returns all supported language codes. */
    suspend fun getLanguages(): List<Language> = get("$base/languages")

    override fun close() { /* HttpClient has no close() in Java 11; GC handles it */ }

    // ── internal ──────────────────────────────────────────────────────────

    private suspend inline fun <reified T> get(url: String): T {
        val request = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .GET()
            .build()

        val response = http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()

        if (response.statusCode() !in 200..299) {
            val message = runCatching {
                json.parseToJsonElement(response.body())
                    .let { it as? kotlinx.serialization.json.JsonObject }
                    ?.get("message")
                    ?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
            }.getOrNull() ?: "HTTP ${response.statusCode()}"

            throw HolidaysApiException(message, response.statusCode(), response.body())
        }

        return json.decodeFromString(response.body())
    }

    private fun buildQuery(vararg pairs: Pair<String, String?>): String {
        val parts = pairs.mapNotNull { (k, v) ->
            if (v == null) null else "$k=${encode(v)}"
        }
        return if (parts.isEmpty()) "" else "?" + parts.joinToString("&")
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8)
}
