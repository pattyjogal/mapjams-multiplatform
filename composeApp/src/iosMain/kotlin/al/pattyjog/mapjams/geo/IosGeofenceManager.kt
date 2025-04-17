package al.pattyjog.mapjams.geo

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject

class IosLocationDelegate(
    private var onLocationUpdate: (CLLocation) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        didUpdateLocations.lastOrNull()?.let { last ->
            if (last is CLLocation) {
                onLocationUpdate(last)
            }
        }
    }
}

class IosGeofenceManager : GeofenceManager(), KoinComponent {
    private val _isTracking = MutableStateFlow(false)
    override val isTracking: Flow<Boolean>
        get() = _isTracking

    private val locationFlow: MutableStateFlow<LatLng?> by inject(named("locationFlow"))

    private val locationManager: CLLocationManager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyBest
        allowsBackgroundLocationUpdates = true
    }

    @OptIn(ExperimentalForeignApi::class)
    private val locationDelegate = IosLocationDelegate { clLocation ->
        clLocation.coordinate.useContents {
            val latLng = LatLng(latitude, longitude)
            locationFlow.tryEmit(latLng)
        }
    }

    init {
        locationManager.delegate = locationDelegate
    }

    override fun startMonitoring() {
        val status = locationManager.authorizationStatus
        if (status != kCLAuthorizationStatusAuthorizedWhenInUse) {
            locationManager.requestWhenInUseAuthorization()
        }

        locationManager.startUpdatingLocation()
        _isTracking.value = true
    }

    override fun stopMonitoring() {
        locationManager.stopUpdatingLocation()
        _isTracking.value = false
    }
}
