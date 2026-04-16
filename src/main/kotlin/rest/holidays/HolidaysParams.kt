package rest.holidays

/**
 * Parameters for [HolidaysClient.getHolidays].
 *
 * @property country ISO 3166 alpha-2 code, e.g. `"US"`. Required.
 * @property year Four-digit year, e.g. `2024`. Required.
 * @property month Month filter (1–12). Optional.
 * @property day Day filter (1–31). Optional.
 * @property type Holiday type(s): `"religious"`, `"national"`, `"local"`. Optional.
 * @property religion Religion code(s) 1–11. Optional.
 * @property region Region/subdivision code(s) — from [HolidaysClient.getCountry]. Optional.
 * @property lang Language code(s) — from [HolidaysClient.getLanguages]. Optional.
 * @property response Response format: `"json"` (default) | `"xml"` | `"yaml"` | `"csv"`. Optional.
 */
data class HolidaysParams(
    val country: String,
    val year: Int,
    val month: Int? = null,
    val day: Int? = null,
    val type: List<String>? = null,
    val religion: List<Int>? = null,
    val region: List<String>? = null,
    val lang: List<String>? = null,
    val response: String? = null,
)
