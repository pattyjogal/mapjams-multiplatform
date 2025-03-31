package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<LatLng>,
    onPolygonUpdate: (List<LatLng>) -> Unit,
    otherRegions: List<Region>
) {
}