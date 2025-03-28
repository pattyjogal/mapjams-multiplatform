package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<LatLng>,
    onPolygonUpdate: (List<LatLng>) -> Unit,
)