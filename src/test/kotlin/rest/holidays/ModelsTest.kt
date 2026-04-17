package rest.holidays

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Holiday deserializes national holiday from API response`() {
        val raw = """
            {
                "country_code": "DE",
                "country_name": "Germany",
                "date": "2026-01-01",
                "name": {"en": "New Year's Day"},
                "isNational": true,
                "isReligious": false,
                "isLocal": false,
                "isEstimate": false,
                "day": {"actual": "Thursday", "observed": "Thursday"},
                "religion": "",
                "regions": []
            }
        """.trimIndent()

        val holiday = json.decodeFromString<Holiday>(raw)

        assertEquals("DE", holiday.countryCode)
        assertEquals("Germany", holiday.countryName)
        assertEquals("2026-01-01", holiday.date)
        assertEquals("New Year's Day", holiday.name["en"])
        assertTrue(holiday.isNational)
        assertFalse(holiday.isReligious)
        assertFalse(holiday.isLocal)
        assertFalse(holiday.isEstimate)
        assertEquals("Thursday", holiday.day.actual)
        assertEquals("Thursday", holiday.day.observed)
        assertEquals("", holiday.religion)
        assertTrue(holiday.regions.isEmpty())
    }

    @Test
    fun `Holiday deserializes religious local holiday with regions`() {
        val raw = """
            {
                "country_code": "DE",
                "country_name": "Germany",
                "date": "2026-01-06",
                "name": {"en": "Epiphany"},
                "isNational": false,
                "isReligious": true,
                "isLocal": true,
                "isEstimate": false,
                "day": {"actual": "Tuesday", "observed": "Tuesday"},
                "religion": "Christianity",
                "regions": ["BW", "BY", "ST"]
            }
        """.trimIndent()

        val holiday = json.decodeFromString<Holiday>(raw)

        assertFalse(holiday.isNational)
        assertTrue(holiday.isReligious)
        assertTrue(holiday.isLocal)
        assertEquals("Christianity", holiday.religion)
        assertEquals(listOf("BW", "BY", "ST"), holiday.regions)
        assertEquals("Epiphany", holiday.name["en"])
    }

    @Test
    fun `Holiday list deserializes from API array response`() {
        val raw = """
            [
                {
                    "country_code": "DE", "country_name": "Germany",
                    "date": "2026-01-01", "name": {"en": "New Year's Day"},
                    "isNational": true, "isReligious": false, "isLocal": false, "isEstimate": false,
                    "day": {"actual": "Thursday", "observed": "Thursday"},
                    "religion": "", "regions": []
                },
                {
                    "country_code": "DE", "country_name": "Germany",
                    "date": "2026-01-06", "name": {"en": "Epiphany"},
                    "isNational": false, "isReligious": true, "isLocal": true, "isEstimate": false,
                    "day": {"actual": "Tuesday", "observed": "Tuesday"},
                    "religion": "Christianity", "regions": ["BW", "BY", "ST"]
                }
            ]
        """.trimIndent()

        val holidays = json.decodeFromString<List<Holiday>>(raw)

        assertEquals(2, holidays.size)
        assertEquals("New Year's Day", holidays[0].name["en"])
        assertEquals("Epiphany", holidays[1].name["en"])
    }

    @Test
    fun `HolidayDay defaults are empty strings`() {
        val day = HolidayDay()
        assertEquals("", day.actual)
        assertEquals("", day.observed)
    }

    @Test
    fun `Holiday defaults are safe`() {
        val holiday = Holiday()
        assertEquals("", holiday.countryCode)
        assertEquals("", holiday.countryName)
        assertEquals("", holiday.date)
        assertTrue(holiday.name.isEmpty())
        assertFalse(holiday.isNational)
        assertFalse(holiday.isReligious)
        assertFalse(holiday.isLocal)
        assertFalse(holiday.isEstimate)
        assertEquals("", holiday.religion)
        assertTrue(holiday.regions.isEmpty())
    }

    @Test
    fun `Country deserializes with subdivisions`() {
        val raw = """
            {
                "name": "United States",
                "alpha2": "US",
                "subdivisions": [
                    {"code": "US-CA", "name": "California"},
                    {"code": "US-NY", "name": "New York"}
                ]
            }
        """.trimIndent()

        val country = json.decodeFromString<Country>(raw)

        assertEquals("United States", country.name)
        assertEquals("US", country.alpha2)
        assertEquals(2, country.subdivisions.size)
        assertEquals("US-CA", country.subdivisions[0].code)
        assertEquals("California", country.subdivisions[0].name)
    }

    @Test
    fun `Language deserializes correctly`() {
        val raw = """{"code": "en", "name": "English"}"""
        val lang = json.decodeFromString<Language>(raw)
        assertEquals("en", lang.code)
        assertEquals("English", lang.name)
    }

    @Test
    fun `Holiday ignores unknown JSON fields`() {
        val raw = """
            {
                "country_code": "US", "country_name": "United States",
                "date": "2026-07-04", "name": {"en": "Independence Day"},
                "isNational": true, "isReligious": false, "isLocal": false, "isEstimate": false,
                "day": {"actual": "Saturday", "observed": "Friday"},
                "religion": "", "regions": [],
                "unknown_future_field": "some_value"
            }
        """.trimIndent()

        val holiday = json.decodeFromString<Holiday>(raw)
        assertEquals("US", holiday.countryCode)
        assertEquals("Friday", holiday.day.observed)
    }
}
