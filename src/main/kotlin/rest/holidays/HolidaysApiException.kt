package rest.holidays

/**
 * Thrown when the holidays.rest API returns a non-2xx HTTP response.
 *
 * @property statusCode HTTP status code returned by the API.
 * @property body Raw response body.
 */
class HolidaysApiException(
    message: String,
    val statusCode: Int,
    val body: String,
) : Exception("holidays.rest API error $statusCode: $message")
