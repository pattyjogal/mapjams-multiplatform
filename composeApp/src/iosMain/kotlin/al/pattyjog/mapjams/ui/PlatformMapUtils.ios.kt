package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.MapKit.MKMapPointForCoordinate
import platform.MapKit.MKMapRect
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygon.Companion.polygonWithCoordinates
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
fun LatLng.toNative(): CValue<CLLocationCoordinate2D> = cValue<CLLocationCoordinate2D> {
    latitude = this@toNative.latitude
    longitude = this@toNative.longitude
}

@OptIn(ExperimentalForeignApi::class)
fun List<CValue<CLLocationCoordinate2D>>.toMKPolygon(): MKPolygon? {
    if (this.isEmpty()) return null
    return polygonWithCoordinates(this.toCArray(), count = this.size.toULong())
}

// Compute a bounding map rect from a list of coords
@OptIn(ExperimentalForeignApi::class)
fun List<CValue<CLLocationCoordinate2D>>.toMKMapRect(): CValue<MKMapRect> {
    if (isEmpty()) return MKMapRectMake(0.0, 0.0, 0.0, 0.0)
    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE
    var maxX = -Double.MAX_VALUE
    var maxY = -Double.MAX_VALUE

    for (coord in this) {
        MKMapPointForCoordinate(coord).useContents {
            minX = min(minX, x); minY = min(minY, y)
            maxX = max(maxX, x); maxY = max(maxY, y)
        }
    }
    return MKMapRectMake(minX, minY, maxX - minX, maxY - minY)
}

@OptIn(ExperimentalForeignApi::class)
class VertexAnnotation(
    val vertexIndex: Int,
    coordinate: CValue<CLLocationCoordinate2D>
) : MKPointAnnotation() {
    init {
        this.setCoordinate(coordinate)
    }
}