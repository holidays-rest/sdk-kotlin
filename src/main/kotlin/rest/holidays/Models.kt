package rest.holidays

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    val name: String = "",
    val date: String = "",
    val type: String = "",
    val country: String = "",
    val region: String? = null,
    val religion: String? = null,
    val language: String? = null,
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
