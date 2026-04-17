package rest.holidays

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class HolidaysApiExceptionTest {

    @Test
    fun `exception message includes status code and message`() {
        val ex = HolidaysApiException("Invalid API key", 401, """{"message":"Invalid API key"}""")
        assertContains(ex.message!!, "401")
        assertContains(ex.message!!, "Invalid API key")
    }

    @Test
    fun `statusCode property is accessible`() {
        val ex = HolidaysApiException("Not Found", 404, "")
        assertEquals(404, ex.statusCode)
    }

    @Test
    fun `body property preserves raw response`() {
        val body = """{"error":"something went wrong"}"""
        val ex = HolidaysApiException("Server Error", 500, body)
        assertEquals(body, ex.body)
    }

    @Test
    fun `exception is instance of Exception`() {
        val ex = HolidaysApiException("Bad Request", 400, "")
        assert(ex is Exception)
    }
}
