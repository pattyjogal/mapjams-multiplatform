package al.pattyjog.mapjams.geo

import kotlinx.coroutines.flow.Flow

abstract class GeofenceManager {
    var regions: List<Region> = emptyList()
    abstract fun startMonitoring()
    abstract fun stopMonitoring()
    abstract val isTracking: Flow<Boolean>
}