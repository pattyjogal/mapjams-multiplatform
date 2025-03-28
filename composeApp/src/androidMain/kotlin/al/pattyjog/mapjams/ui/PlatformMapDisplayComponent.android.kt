package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun PlatformMapDisplayComponent(
    regions: List<Region>, currentLocation: LatLng
) {
    val currentLatLng = com.google.android.gms.maps.model.LatLng(currentLocation.latitude, currentLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
    }

    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
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
                points = region.polygon.map { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
            )
        }
    }
}