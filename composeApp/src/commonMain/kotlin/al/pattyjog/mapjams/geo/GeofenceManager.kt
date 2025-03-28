package al.pattyjog.mapjams.geo

import kotlinx.coroutines.flow.Flow

interface GeofenceManager {
    fun startMonitoring()
    fun stopMonitoring()
    val isTracking: Flow<Boolean>
    fun setRegions(regions: List<Region>)
    fun onEnterRegion(regionId: String)
    fun onExitRegion(regionId: String)
}