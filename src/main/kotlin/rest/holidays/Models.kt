package rest.holidays

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HolidayDay(
    val actual: String = "",
    val observed: String = "",
)

@Serializable
data class Holiday(
    @SerialName("country_code") val countryCode: String = "",
    @SerialName("country_name") val countryName: String = "",
    val date: String = "",
    val name: Map<String, String> = emptyMap(),
    @SerialName("isNational") val isNational: Boolean = false,
    @SerialName("isReligious") val isReligious: Boolean = false,
    @SerialName("isLocal") val isLocal: Boolean = false,
    @SerialName("isEstimate") val isEstimate: Boolean = false,
    val day: HolidayDay = HolidayDay(),
    val religion: String = "",
    val regions: List<String> = emptyList(),
)

@Serializable
data class Subdivision(
    val code: String = "",
    val name: String = "",
)

@Serializable
data class Country(
    val name: String = "",
    @SerialName("alpha2") val alpha2: String = "",
    val subdivisions: List<Subdivision> = emptyList(),
)

@Serializable
data class Language(
    val code: String = "",
    val name: String = "",
)
