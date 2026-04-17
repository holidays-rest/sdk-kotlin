package rest.holidays

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HolidaysClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: HolidaysClient

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = HolidaysClient(apiKey = "test-key", baseUrl = server.url("/").toString().trimEnd('/'))
    }

    @AfterTest
    fun tearDown() {
        client.close()
        server.shutdown()
    }

    // ── getHolidays ───────────────────────────────────────────────────────

    @Test
    fun `getHolidays returns parsed holiday list`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                [
                    {
                        "country_code": "DE", "country_name": "Germany",
                        "date": "2026-01-01", "name": {"en": "New Year's Day"},
                        "isNational": true, "isReligious": false, "isLocal": false, "isEstimate": false,
                        "day": {"actual": "Thursday", "observed": "Thursday"},
                        "religion": "", "regions": []
                    }
                ]
            """.trimIndent()))

        val holidays = client.getHolidays(HolidaysParams(country = "DE", year = 2026))

        assertEquals(1, holidays.size)
        assertEquals("DE", holidays[0].countryCode)
        assertEquals("New Year's Day", holidays[0].name["en"])
        assertTrue(holidays[0].isNational)
    }

    @Test
    fun `getHolidays sends correct query parameters`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        client.getHolidays(HolidaysParams(
            country = "US",
            year = 2024,
            month = 7,
            day = 4,
            type = listOf("national"),
            region = listOf("US-CA"),
        ))

        val request = server.takeRequest()
        val query = request.requestUrl!!.query!!
        assertTrue(query.contains("country=US"))
        assertTrue(query.contains("year=2024"))
        assertTrue(query.contains("month=7"))
        assertTrue(query.contains("day=4"))
        assertTrue(query.contains("type=national"))
        assertTrue(query.contains("region=US-CA"))
    }

    @Test
    fun `getHolidays sends Authorization header`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        client.getHolidays(HolidaysParams(country = "US", year = 2024))

        val request = server.takeRequest()
        assertEquals("Bearer test-key", request.getHeader("Authorization"))
    }

    @Test
    fun `getHolidays throws HolidaysApiException on 401`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(401)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"message":"Invalid API key"}"""))

        val ex = assertFailsWith<HolidaysApiException> {
            client.getHolidays(HolidaysParams(country = "US", year = 2024))
        }
        assertEquals(401, ex.statusCode)
    }

    @Test
    fun `getHolidays throws HolidaysApiException on 500`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(500)
            .setBody("""{"message":"Internal Server Error"}"""))

        val ex = assertFailsWith<HolidaysApiException> {
            client.getHolidays(HolidaysParams(country = "US", year = 2024))
        }
        assertEquals(500, ex.statusCode)
    }

    @Test
    fun `getHolidays throws on blank country`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            client.getHolidays(HolidaysParams(country = " ", year = 2024))
        }
    }

    @Test
    fun `getHolidays handles multiple regions and types`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        client.getHolidays(HolidaysParams(
            country = "US",
            year = 2024,
            type = listOf("national", "religious"),
            region = listOf("US-CA", "US-NY"),
        ))

        val request = server.takeRequest()
        val query = request.requestUrl!!.query!!
        assertTrue(query.contains("type=national%2Creligious") || query.contains("type=national,religious"))
        assertTrue(query.contains("region=US-CA%2CUS-NY") || query.contains("region=US-CA,US-NY"))
    }

    // ── getCountries ──────────────────────────────────────────────────────

    @Test
    fun `getCountries returns parsed country list`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""[{"name":"Germany","alpha2":"DE","subdivisions":[]}]"""))

        val countries = client.getCountries()

        assertEquals(1, countries.size)
        assertEquals("DE", countries[0].alpha2)
        assertEquals("Germany", countries[0].name)
    }

    // ── getCountry ────────────────────────────────────────────────────────

    @Test
    fun `getCountry returns country with subdivisions`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "name": "United States",
                    "alpha2": "US",
                    "subdivisions": [
                        {"code": "US-CA", "name": "California"},
                        {"code": "US-NY", "name": "New York"}
                    ]
                }
            """.trimIndent()))

        val country = client.getCountry("US")

        assertEquals("US", country.alpha2)
        assertEquals(2, country.subdivisions.size)
        assertEquals("US-CA", country.subdivisions[0].code)
    }

    @Test
    fun `getCountry includes country code in URL path`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"name":"Germany","alpha2":"DE","subdivisions":[]}"""))

        client.getCountry("DE")

        val request = server.takeRequest()
        assertTrue(request.path!!.contains("DE"))
    }

    @Test
    fun `getCountry throws on blank countryCode`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            client.getCountry("")
        }
    }

    @Test
    fun `getCountry throws HolidaysApiException on 404`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(404)
            .setBody("""{"message":"Country not found"}"""))

        val ex = assertFailsWith<HolidaysApiException> {
            client.getCountry("XX")
        }
        assertEquals(404, ex.statusCode)
    }

    // ── getLanguages ──────────────────────────────────────────────────────

    @Test
    fun `getLanguages returns parsed language list`() = runTest {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""[{"code":"en","name":"English"},{"code":"de","name":"German"}]"""))

        val languages = client.getLanguages()

        assertEquals(2, languages.size)
        assertEquals("en", languages[0].code)
        assertEquals("English", languages[0].name)
    }

    // ── constructor validation ─────────────────────────────────────────────

    @Test
    fun `blank apiKey throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            HolidaysClient(apiKey = "  ")
        }
    }

    @Test
    fun `empty apiKey throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> {
            HolidaysClient(apiKey = "")
        }
    }
}
