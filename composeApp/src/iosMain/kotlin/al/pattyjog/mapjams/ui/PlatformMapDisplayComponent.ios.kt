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
import platform.MapKit.MKClusterAnnotation
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKOverlayView
import platform.MapKit.MKPointOfInterestCategory
import platform.MapKit.MKPointOfInterestFilter
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygonRenderer
import platform.MapKit.MKStandardMapConfiguration
import platform.MapKit.MKUserLocation
import platform.MapKit.MKUserLocationView
import platform.MapKit.MKUserTrackingButton
import platform.MapKit.addOverlay
import platform.UIKit.NSLayoutConstraint
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
    UIKitView(
        factory = {
            val mapView = MKMapView()
            mapView.delegate = object : MKMapViewDelegateProtocol, NSObject() {
                override fun mapView(mapView: MKMapView, didUpdateUserLocation: MKUserLocation) {
                    val coord = didUpdateUserLocation.coordinate
                    val region = MKCoordinateRegionMakeWithDistance(
                        coord,    // center on the user
                        500.0,
                        500.0
                    )
                    mapView.setRegion(region, animated = true)
                }

                // Stops the user popup from showing if blue dot is clicked
                @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                @ObjCSignatureOverride
                override fun mapView(
                    mapView: MKMapView,
                    didSelectAnnotationView: MKAnnotationView
                ) {
                    when (val annotation = didSelectAnnotationView.annotation) {
                        is MKUserLocation -> {
                            mapView.deselectAnnotation(annotation, animated = false)
                        }
                    }
                }

                @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                @ObjCSignatureOverride
                override fun mapView(
                    mapView: MKMapView,
                    rendererForOverlay: MKOverlayProtocol
                ): MKOverlayRenderer {
                    return when (val overlay = rendererForOverlay as? MKPolygon) {
                        is MKPolygon -> {
                            val renderer = MKPolygonRenderer(overlay)
                            renderer.fillColor = platform.UIKit.UIColor.orangeColor()
                                .colorWithAlphaComponent(0.2) // Example
                            renderer.strokeColor = platform.UIKit.UIColor.orangeColor()
                            renderer.lineWidth = 2.0
                            renderer
                        }

                        else -> MKOverlayRenderer(overlay!!)
                    }
                }
            }
            mapView.showsUserLocation = true
            regions.forEach { region ->
                region.polygon.map { it.toNative() }.toMKPolygon()?.let { mapView.addOverlay(it) }
            }
            val configuration = MKStandardMapConfiguration()
            configuration.pointOfInterestFilter =
                MKPointOfInterestFilter(includingCategories = emptyList<MKPointOfInterestCategory>())
            mapView.preferredConfiguration = configuration

            val trackingButton = MKUserTrackingButton.userTrackingButtonWithMapView(mapView).apply {
                translatesAutoresizingMaskIntoConstraints = false
            }

            mapView.addSubview(trackingButton)

            val guide = mapView.safeAreaLayoutGuide
            NSLayoutConstraint.activateConstraints(listOf(
                trackingButton.trailingAnchor().constraintEqualToAnchor(guide.trailingAnchor, constant = -16.0),
                trackingButton.bottomAnchor().constraintEqualToAnchor(guide.bottomAnchor, constant = -16.0)
            ))

            mapView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

