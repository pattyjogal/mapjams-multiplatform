package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectZero
import platform.CoreLocation.CLLocationCoordinate2D
import platform.Foundation.NSSelectorFromString
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygon.Companion.polygonWithCoordinates
import platform.MapKit.MKPolygonRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlay
import platform.MapKit.removeOverlays
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.systemBlueColor
import platform.darwin.NSObject

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



@OptIn(ExperimentalForeignApi::class)
class MapDelegate(
    private val mapView: MKMapView,
    private val onPolygonUpdate: (List<LatLng>) -> Unit
) : NSObject(), MKMapViewDelegateProtocol {
    private val _nativePolygon = mutableStateListOf<CValue<CLLocationCoordinate2D>>()
    val nativePolygon: List<CValue<CLLocationCoordinate2D>> get() = _nativePolygon

    fun setInitial(native: List<CValue<CLLocationCoordinate2D>>) {
        _nativePolygon.clear()
        _nativePolygon += native
        redrawOverlays()
    }

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    @ObjCAction
    fun handleLongPress(gesture: UILongPressGestureRecognizer) {
        if (gesture.state == UIGestureRecognizerStateEnded) {
            val pt = gesture.locationInView(mapView)
            val coord = mapView.convertPoint(pt, toCoordinateFromView = mapView)
            _nativePolygon += coord
            onPolygonUpdate(_nativePolygon.map { x -> x.useContents { LatLng(latitude, longitude) } })
            redrawOverlays()
        }
    }

    var otherOverlays: List<MKPolygon> = emptyList()
        set(value) {
            field = value
            redrawOverlays()
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun redrawOverlays() {
        // snapshot overlays
        mapView.removeOverlays(mapView.overlays)
        // Add user polygon
        nativePolygon.toMKPolygon()?.let { mapView.addOverlay(it) }
        // Add others
        otherOverlays.forEach { mapView.addOverlay(it) }
    }

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun mapView(
        mapView: MKMapView,
        rendererForOverlay: MKOverlayProtocol
    ): MKOverlayRenderer {
        return if (rendererForOverlay is MKPolygon) {
            val rend = MKPolygonRenderer(polygon = rendererForOverlay)
            rend.fillColor = platform.UIKit.UIColor.systemBlueColor().colorWithAlphaComponent(0.3)
            rend.strokeColor = platform.UIKit.UIColor.systemBlueColor()
            rend.lineWidth = 2.0
            rend
        } else {
            MKOverlayRenderer(overlay = rendererForOverlay)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<LatLng>,
    onPolygonUpdate: (List<LatLng>) -> Unit,
    otherRegions: List<Region>
) {
    // Convert shared initialPolygon and otherRegions into native coords/polygons
    val nativeInitial = remember { initialPolygon.map { it.toNative() } }
    val nativeOthers = remember(otherRegions) {
        otherRegions.mapNotNull { region ->
            region.polygon.map { it.toNative() }.toMKPolygon()
        }
    }

    // Remember single mapView + delegate for lifetime of this Composable
    val mapView = remember { MKMapView() }
    val delegate = remember {
        MapDelegate(mapView, onPolygonUpdate = onPolygonUpdate).apply {
            setInitial(nativeInitial)
            otherOverlays = nativeOthers
        }
    }

    // Set up mapView once
    LaunchedEffect(mapView) {
        mapView.delegate = delegate
        // Add long‑press recognizer
        val longPress = UILongPressGestureRecognizer(target = delegate,
            action = NSSelectorFromString("handleLongPress:"))
        mapView.addGestureRecognizer(longPress)
        // Center map initially on first vertex (or skip)
        nativeInitial.firstOrNull()?.let { center ->
            val region = MKCoordinateRegionMakeWithDistance(center, 800.0, 800.0)
            mapView.setRegion(region, animated = false)
        }
    }

    // Whenever otherRegions changes, update delegate
    LaunchedEffect(nativeOthers) {
        delegate.otherOverlays = nativeOthers
    }

    // Host the MKMapView inside Compose on iOS
    UIKitView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}