package al.pattyjog.mapjams.geo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

class AndroidGeofenceManger(
    private val context: Context,
): GeofenceManager {
    private var locationService: LocationService? = null
    private val _isTracking = MutableStateFlow(false)
    override val isTracking: Flow<Boolean>
        get() = _isTracking


    override val locationUpdates = emptyFlow<LatLng>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            locationService = (service as? LocationService.LocalBinder)?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
        }
    }

    override fun startMonitoring() {
        if (!_isTracking.value) {
            val intent = Intent(context, LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            _isTracking.value = true
        }
    }

    override fun stopMonitoring() {
        if (_isTracking.value) {
            val intent = Intent(context, LocationService::class.java)
            context.unbindService(serviceConnection)
            context.stopService(intent)
            _isTracking.value = false
        }
    }

    override fun setRegions(regions: List<Region>) {
        TODO("Not yet implemented")
    }

    override fun onEnterRegion(regionId: String) {
        TODO("Not yet implemented")
    }

    override fun onExitRegion(regionId: String) {
        TODO("Not yet implemented")
    }
}