package al.pattyjog.mapjams.geo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidGeofenceManger(
    private val context: Context,
): GeofenceManager() {
    private var locationService: LocationService? = null
    private val _isTracking = MutableStateFlow(false)
    override val isTracking: Flow<Boolean>
        get() = _isTracking

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            locationService = (service as? LocationService.LocalBinder)?.getService()
            Log.v("LocationService", "Service connected")
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
            Handler(Looper.getMainLooper()).postDelayed({
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                _isTracking.value = true
                Logger.d { "Started monitoring user location" }
            }, 500)
        }
    }

    override fun stopMonitoring() {
        if (_isTracking.value) {
            val intent = Intent(context, LocationService::class.java)
            context.unbindService(serviceConnection)
            context.stopService(intent)
            _isTracking.value = false
            Logger.d { "Stopped monitoring user location" }
        }
    }
}