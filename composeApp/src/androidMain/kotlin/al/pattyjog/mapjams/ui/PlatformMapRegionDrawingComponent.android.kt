package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionResultCallback
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
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
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = isFineLocationPermissionGranted ))
    }

    LaunchedEffect(isFineLocationPermissionGranted) {
        if (!isFineLocationPermissionGranted) {
            requestPermission()
        }
    }
    // GoogleMap composable provided by the Maps Compose library.
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        properties = properties,
        cameraPositionState = cameraPositionState,
        onMapLongClick = {
            polygon += it
        }
    ) {
        // You can add markers, polygons, or custom overlays for drawing.
        Polygon(
            points = polygon,
            fillColor = Color(55, 55, 55, 30)
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
                    Log.d("Polygon", "NEW ONE")
                    onPolygonUpdate(polygon.map { al.pattyjog.mapjams.geo.LatLng(it.latitude, it.longitude) })
                }
            )
        }
    }
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