package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformMapDisplayComponent(
    regions: List<Region>,
    currentLocation: LatLng
)