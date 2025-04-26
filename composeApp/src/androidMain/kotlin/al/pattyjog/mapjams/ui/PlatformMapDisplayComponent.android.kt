package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionResultCallback
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.compose.getKoin

@Composable
actual fun PlatformMapDisplayComponent(
    regions: List<Region>, currentLocation: LatLng
) {
    val currentLatLng = com.google.android.gms.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
    }

    val koin = getKoin()
    var isFineLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isLocationPermissionGranted()
        )
    }

    fun requestPermission() {
        koin.get<PermissionBridge>()
            .requestLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    isFineLocationPermissionGranted =
                        koin.get<PermissionBridge>().isLocationPermissionGranted()
                }

                override fun onPermissionDenied(
                    isPermanentDenied: Boolean
                ) {
                    isFineLocationPermissionGranted =
                        koin.get<PermissionBridge>().isLocationPermissionGranted()
                }
            })
    }

    LaunchedEffect(isFineLocationPermissionGranted) {
        if (!isFineLocationPermissionGranted) {
            requestPermission()
        }
    }


    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = isFineLocationPermissionGranted))
    }


    LaunchedEffect(currentLatLng) {
        currentLatLng.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f),
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = properties
    ) {
        regions.map { region ->
            Polygon(
                points = region.polygon.map { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) },
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5F),
                strokeColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}