package rest.holidays

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HolidaysParamsTest {

    @Test
    fun `required fields only`() {
        val params = HolidaysParams(country = "US", year = 2024)
        assertEquals("US", params.country)
        assertEquals(2024, params.year)
        assertNull(params.month)
        assertNull(params.day)
        assertNull(params.type)
        assertNull(params.religion)
        assertNull(params.region)
        assertNull(params.lang)
        assertNull(params.response)
    }

    @Test
    fun `all optional fields populated`() {
        val params = HolidaysParams(
            country = "DE",
            year = 2026,
            month = 1,
            day = 6,
            type = listOf("national", "religious"),
            religion = listOf(1, 2),
            region = listOf("BW", "BY"),
            lang = listOf("en", "de"),
            response = "json",
        )
        assertEquals("DE", params.country)
        assertEquals(1, params.month)
        assertEquals(6, params.day)
        assertEquals(listOf("national", "religious"), params.type)
        assertEquals(listOf(1, 2), params.religion)
        assertEquals(listOf("BW", "BY"), params.region)
        assertEquals(listOf("en", "de"), params.lang)
        assertEquals("json", params.response)
    }

    @Test
    fun `copy produces independent instance`() {
        val original = HolidaysParams(country = "US", year = 2024)
        val copy = original.copy(country = "DE", year = 2026)
        assertEquals("DE", copy.country)
        assertEquals(2026, copy.year)
        assertEquals("US", original.country)
    }
}
