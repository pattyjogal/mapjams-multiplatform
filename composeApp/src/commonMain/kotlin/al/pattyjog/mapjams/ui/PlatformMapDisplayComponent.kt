package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformMapDisplayComponent(
    regions: List<LatLng>,
    currentLocation: LatLng
)