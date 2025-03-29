package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionResultCallback
import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.GeofenceManager
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun Home() {
    var checked by remember { mutableStateOf(false) }
    val geofenceManager: GeofenceManager = koinInject()
    val locationViewModel: LocationViewModel = koinInject()
    val mapViewModel: MapViewModel = koinInject()
    val isTrackingLocation by geofenceManager.isTracking.collectAsState(false)
    val location by locationViewModel.locationFlow.collectAsState() // TODO: Tabbing back goes back to this default
    val activeMap by locationViewModel.activeMapFlow.collectAsState()
    val activeRegion by locationViewModel.regionFlow.collectAsState()

    val koin = getKoin()
    var isFineLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isLocationPermissionGranted()
        )
    }
    var isBackgroundLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
        )
    }
    val isDocumentAccessPermissionNeeded by remember {
        mutableStateOf(
            activeMap?.regions?.any { it.musicSource is MusicSource.Local } == true
        )
    }
    var isDocumentAccessPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
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
        koin.get<PermissionBridge>()
            .requestBackgroundLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    isBackgroundLocationPermissionGranted =
                        koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
                }

                override fun onPermissionDenied(
                    isPermanentDenied: Boolean
                ) {
                    isBackgroundLocationPermissionGranted =
                        koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
                }
            })

        if (isDocumentAccessPermissionNeeded) {
            koin.get<PermissionBridge>()
                .requestDocumentAccessPermission(object : PermissionResultCallback {
                    override fun onPermissionGranted() {
                        isDocumentAccessPermissionGranted =
                            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
                    }

                    override fun onPermissionDenied(
                        isPermanentDenied: Boolean
                    ) {
                        isDocumentAccessPermissionGranted =
                            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
                    }
                })
        }
    }

    LaunchedEffect(checked, isFineLocationPermissionGranted, isBackgroundLocationPermissionGranted, isDocumentAccessPermissionGranted, isDocumentAccessPermissionNeeded) {
        if (isFineLocationPermissionGranted && isBackgroundLocationPermissionGranted && (!isDocumentAccessPermissionNeeded || isDocumentAccessPermissionGranted)) {
            if (checked) {
                geofenceManager.startMonitoring()
            } else {
                geofenceManager.stopMonitoring()
            }
        }

    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (location != null) {
            PlatformMapDisplayComponent(
                regions = activeMap?.regions ?: emptyList(),
                currentLocation = location!! // TODO: This feels hacky
            )
        } else {
            Text("Cannot display map yet", modifier = Modifier.fillMaxSize())
        }
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Switch(
                onCheckedChange = {
                    checked = it
                    if (!isFineLocationPermissionGranted || !isBackgroundLocationPermissionGranted || (isDocumentAccessPermissionNeeded && !isDocumentAccessPermissionGranted)) {
                        requestPermission()
                    }
                },
                checked = checked
            )
            Text("Permissions: ${isFineLocationPermissionGranted} and ${isBackgroundLocationPermissionGranted}")
            Text("Region ${activeRegion?.name}")
        }
    }


}