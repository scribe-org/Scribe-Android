import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataContract(
    val numbers: Map<String, String>,
    val genders: Genders,
    val conjugations: Map<String, Conjugation>,
)

@Serializable
data class Genders(
    val canonical: List<String>,
    val feminines: List<String>,
    val masculines: List<String>,
    val commons: List<String>,
    val neuters: List<String>,
)

@Serializable
data class Conjugation(
    val title: String = "",
    @SerialName("1") val firstPerson: Map<String, String>? = null,
    @SerialName("2") val secondPerson: Map<String, String>? = null,
    @SerialName("3") val thirdPersonSingular: Map<String, String>? = null,
    @SerialName("4") val firstPersonPlural: Map<String, String>? = null,
    @SerialName("5") val secondPersonPlural: Map<String, String>? = null,
    @SerialName("6") val thirdPersonPlural: Map<String, String>? = null,
)
