package al.pattyjog.mapjams.geo

interface GeofenceManager {
    fun startMonitoring()
    fun stopMonitoring()
    fun setRegions(regions: List<Region>)
    fun onEnterRegion(regionId: String)
    fun onExitRegion(regionId: String)
}