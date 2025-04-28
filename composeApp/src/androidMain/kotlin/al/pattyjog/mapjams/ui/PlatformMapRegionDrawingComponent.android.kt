package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionResultCallback
import al.pattyjog.mapjams.PlatformHaptic
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.koin.compose.getKoin

@Composable
actual fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<al.pattyjog.mapjams.geo.LatLng>,
    onPolygonUpdate: (List<al.pattyjog.mapjams.geo.LatLng>) -> Unit,
    otherRegions: List<Region>,
    isLocked: Boolean,
) {
    var polygon: List<LatLng> by remember { mutableStateOf(initialPolygon.map { LatLng(it.latitude, it.longitude) }) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 14f)
    }
    val koin = getKoin()
    var isFineLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isLocationPermissionGranted()
        )
    }

    val haptic = koin.get<PlatformHaptic>()

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

    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = isFineLocationPermissionGranted))
    }

    LaunchedEffect(Unit) {
        if (initialPolygon.size >= 2) {
            val update = CameraUpdateFactory.newLatLngBounds(computeBounds(initialPolygon.map {
                LatLng(
                    it.latitude,
                    it.longitude
                )
            })!!, 100)
            cameraPositionState.animate(update)
        }
    }

    LaunchedEffect(isFineLocationPermissionGranted) {
        if (!isFineLocationPermissionGranted) {
            requestPermission()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = properties,
        cameraPositionState = cameraPositionState,
        onMapLongClick = {
            polygon += it
            haptic.shortBuzz()
        },
        contentPadding = PaddingValues(bottom = 64.dp),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = !isLocked,
            scrollGesturesEnabled = !isLocked,
            tiltGesturesEnabled = !isLocked,
            rotationGesturesEnabled = !isLocked,
            compassEnabled = !isLocked,
        )
    ) {
        Polygon(
            points = polygon,
            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5F),
            strokeColor = MaterialTheme.colorScheme.primary
        )
        polygon.mapIndexed { i, position ->
            DraggableMarkerWithEffect(
                initialPosition = position,
                onDragChange = { newPosition ->
                    polygon = polygon.toMutableList().apply {
                        set(i, newPosition)
                    }
                },
                onDragEnd = {
                    onPolygonUpdate(polygon.map { al.pattyjog.mapjams.geo.LatLng(it.latitude, it.longitude) })
                }
            )
        }
        otherRegions.map { region ->
            Polygon(
                points = region.polygon.map { LatLng(it.latitude, it.longitude) },
                fillColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3F),
                strokeColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun computeBounds(locations: List<LatLng>): LatLngBounds? {
    if (locations.isEmpty()) return null

    val minLat = locations.minOf { it.latitude }
    val minLng = locations.minOf { it.longitude }
    val maxLat = locations.maxOf { it.latitude }
    val maxLng = locations.maxOf { it.longitude }

    val southwest = LatLng(minLat, minLng)
    val northeast = LatLng(maxLat, maxLng)

    return LatLngBounds(southwest, northeast)
}

@OptIn(FlowPreview::class)
@Composable
fun DraggableMarkerWithEffect(
    initialPosition: LatLng,
    onDragChange: (LatLng) -> Unit,
    onDragEnd: (LatLng) -> Unit
) {
    // Create a marker state from the current markerPosition.
    val markerState = remember { MarkerState(initialPosition) }

    // Observe changes in markerState.position using LaunchedEffect.
    // When it changes, we update our markerPosition and call onDragEnd.
    LaunchedEffect(markerState.position) {
        onDragChange(markerState.position)
        snapshotFlow {
            markerState.position
        }.debounce(500)
            .collect {
                onDragEnd(markerState.position)
            }
    }
    Marker(
        state = markerState,
        draggable = true,
    )
}