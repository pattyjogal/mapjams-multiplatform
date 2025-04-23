package al.pattyjog.mapjams.geo

import al.pattyjog.mapjams.MainActivity
import al.pattyjog.mapjams.R
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class LocationService : Service() {
    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    private val binder = LocalBinder()

    private val locationFlow: MutableStateFlow<LatLng?> by inject(named("locationFlow"))
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var locationManager: LocationManager
    private val locationListener = LocationListener {
        serviceScope.launch {
            Log.d("LocationListener", "EMIT ${it.latitude}, ${it.longitude}; accuracy ${it.accuracy}m; provided by ${it.provider}")
            locationFlow.emit(LatLng(it.latitude, it.longitude))
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w("GPS", "GPS Provider is disabled")
        }

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("LocationService", "GPS provider enabled: $gpsEnabled")

        val hasPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Log.d("LocationService", "Fine location permission granted: $hasPermission")

        try {
            locationManager.requestLocationUpdates(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) LocationManager.FUSED_PROVIDER else LocationManager.GPS_PROVIDER,
                2000L,
                1f,
                locationListener
            )
        } catch (e: SecurityException) {
            Log.e("GPS", "Stop bihh", e)
        }

        Log.v("LocationService", "manager set")

        return START_STICKY
    }

    override fun onDestroy() {
        Log.w("LocationService", "onDestroy")
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }

    private fun createNotification(): Notification {
        val channelId = "mapjams_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mapjams Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                channel
            )
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking location")
            .setContentText("Your location is being tracked in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?) = binder
}