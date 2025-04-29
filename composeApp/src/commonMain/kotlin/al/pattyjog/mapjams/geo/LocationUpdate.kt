package al.pattyjog.mapjams.geo

data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val elapsedRealtimeNanos: Long,
)