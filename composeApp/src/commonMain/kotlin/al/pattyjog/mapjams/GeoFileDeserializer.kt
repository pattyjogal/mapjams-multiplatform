package al.pattyjog.mapjams

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeoJson(
    val type: String, val features: List<Feature>
)

@Serializable
data class Feature(
    val type: String, val properties: Properties, val geometry: Geometry
)

@Serializable
data class Geometry(
    val coordinates: List<List<List<Double>>>, val type: String
)

@Serializable
data class Properties(
    val name: String?
)

inline fun <reified T> decodeJson(
    jsonStr: String, json: Json = Json { ignoreUnknownKeys = true }
): T = json.decodeFromString<T>(jsonStr)