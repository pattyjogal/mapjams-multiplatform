package al.pattyjog.mapjams.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
actual fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<al.pattyjog.mapjams.geo.LatLng>,
    onPolygonUpdate: (List<al.pattyjog.mapjams.geo.LatLng>) -> Unit
) {
    var polygon: List<LatLng> by remember { mutableStateOf(initialPolygon.map { LatLng(it.latitude, it.longitude) }) }
    val center = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 10f)
    }

    // GoogleMap composable provided by the Maps Compose library.
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
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
                onDragEnd = { newPosition ->
                    Log.d("Huh", "I supposedly updated")
                    polygon = polygon.toMutableList().apply {
                        set(i, newPosition)
                    }
                }
            )
        }

        // TODO: Add drawing logic (e.g., drag gestures, polygon overlays) as needed.
    }
}

@Composable
fun DraggableMarkerWithEffect(
    initialPosition: LatLng,
    onDragEnd: (LatLng) -> Unit
) {
    // Hold the marker position in a mutable state
    var markerPosition by remember { mutableStateOf(initialPosition) }
    // Create a marker state from the current markerPosition.
    val markerState = rememberUpdatedMarkerState(position = markerPosition)

    // Observe changes in markerState.position using LaunchedEffect.
    // When it changes, we update our markerPosition and call onDragEnd.
    LaunchedEffect(markerState.position) {
        // TODO: Implement here
        if (markerState.position != markerPosition) {
            markerPosition = markerState.position
            onDragEnd(markerPosition)
        }
    }

    Marker(
        state = markerState,
        draggable = true,
        // Note: There's no onDragEnd parameter here.
        // We rely on LaunchedEffect above to detect when the drag has ended.
        onClick = { true }
    )

}