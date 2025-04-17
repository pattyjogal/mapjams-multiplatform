package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CStructVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.interpretPointed
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKOverlayView
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygonRenderer
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
inline fun <reified T : CStructVar> List<CValue<T>>.toCArray(): CArrayPointer<T> {
    val array = nativeHeap.allocArray<T>(this.size)
    val typeSize = sizeOf<T>()
    this.forEachIndexed { index, cValue ->
        cValue.place(interpretPointed<T>(array.rawValue + index * typeSize).ptr)
    }
    return array
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMapDisplayComponent(
    regions: List<Region>,
    currentLocation: LatLng
) {
    val mkPolygons = regions.map { region ->

        val coordinates = region.polygon.map { latLng ->
            cValue<CLLocationCoordinate2D> {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
        }.toCArray()
        platform.MapKit.MKPolygon.polygonWithCoordinates(coordinates, count = region.polygon.size.toULong())
    }

    val nativeLocation = cValue<CLLocationCoordinate2D> {
        latitude = currentLocation.latitude
        longitude = currentLocation.longitude
    }

    UIKitView(
        factory = {
            val mapView = MKMapView()
            mapView.delegate = object : MKMapViewDelegateProtocol, NSObject() {
                @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                @ObjCSignatureOverride
                override fun mapView(
                    mapView: MKMapView,
                    rendererForOverlay: MKOverlayProtocol
                ): MKOverlayRenderer {
                    return when (val overlay = rendererForOverlay as? MKPolygon) {
                        is MKPolygon -> {
                            val renderer = MKPolygonRenderer(overlay)
                            renderer.fillColor = platform.UIKit.UIColor.blueColor().colorWithAlphaComponent(0.2) // Example
                            renderer.strokeColor = platform.UIKit.UIColor.blueColor()
                            renderer.lineWidth = 2.0
                            renderer
                        }
                        else -> MKOverlayRenderer(overlay!!)
                    }
                }
            }
            mapView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

