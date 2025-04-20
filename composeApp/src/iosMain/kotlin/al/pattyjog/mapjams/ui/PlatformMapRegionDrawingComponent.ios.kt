package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.Foundation.NSSelectorFromString
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKAnnotationViewDragState
import platform.MapKit.MKAnnotationViewDragStateCanceling
import platform.MapKit.MKAnnotationViewDragStateEnding
import platform.MapKit.MKAnnotationViewDragStateNone
import platform.MapKit.MKCoordinateRegionForMapRect
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPinAnnotationView
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygonRenderer
import platform.MapKit.MKUserLocation
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.systemBlueColor
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class MapDelegate(
    private val mapView: MKMapView,
    private val onPolygonUpdate: (List<LatLng>) -> Unit
) : NSObject(), MKMapViewDelegateProtocol {
    private val _nativePolygon = mutableStateListOf<CValue<CLLocationCoordinate2D>>()
    val nativePolygon: List<CValue<CLLocationCoordinate2D>> get() = _nativePolygon

    private var hasCentered = false

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
            onPolygonUpdate(_nativePolygon.map { x ->
                x.useContents {
                    LatLng(
                        latitude,
                        longitude
                    )
                }
            })
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
        mapView.removeAnnotations(mapView.annotations)

        // Add user polygon
        nativePolygon.toMKPolygon()?.let { mapView.addOverlay(it) }
        nativePolygon.forEachIndexed { idx, coord ->
            val ann = VertexAnnotation(idx, coord)
            mapView.addAnnotation(ann)
        }
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

    override fun mapView(mapView: MKMapView, didUpdateUserLocation: MKUserLocation) {
        if (!hasCentered) {
            hasCentered = true
            val coord = didUpdateUserLocation.coordinate
            val region = MKCoordinateRegionMakeWithDistance(
                coord,    // center on the user
                1_000.0,  // 1 km north‑south
                1_000.0   // 1 km east‑west
            )
            mapView.setRegion(region, animated = true)
        }
    }

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun mapView(
        mapView: MKMapView,
        viewForAnnotation: MKAnnotationProtocol
    ): MKAnnotationView? {
        if (viewForAnnotation is VertexAnnotation) {
            val id = "vertexPin"
            val pin = (mapView.dequeueReusableAnnotationViewWithIdentifier(id)
                    as? MKPinAnnotationView)
                ?: MKPinAnnotationView(annotation = viewForAnnotation, reuseIdentifier = id).apply {
                    draggable = true
                    canShowCallout = false
                }
            pin.annotation = viewForAnnotation
            return pin
        }
        return null
    }

    override fun mapView(
        mapView: MKMapView,
        annotationView: MKAnnotationView,
        didChangeDragState: MKAnnotationViewDragState,
        fromOldState: MKAnnotationViewDragState
    ) {
        // We're only interested when dragging ends
        if (annotationView.annotation is VertexAnnotation &&
            (didChangeDragState == MKAnnotationViewDragStateEnding ||
                    didChangeDragState == MKAnnotationViewDragStateCanceling)
        ) {
            val vertexAnn = annotationView.annotation as VertexAnnotation
            val newCoord = vertexAnn.coordinate
            // Update our state list in-place
            _nativePolygon[vertexAnn.vertexIndex] = newCoord
            // Notify shared code
            onPolygonUpdate(_nativePolygon.map { it.useContents { LatLng(latitude, longitude) } })
            // Redraw overlay to follow moved point
            redrawOverlays()
            annotationView.dragState = MKAnnotationViewDragStateNone
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformMapRegionDrawingComponent(
    initialPolygon: List<LatLng>,
    onPolygonUpdate: (List<LatLng>) -> Unit,
    otherRegions: List<Region>,
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
        mapView.showsUserLocation = true
        // Add long‑press recognizer
        val longPress = UILongPressGestureRecognizer(
            target = delegate,
            action = NSSelectorFromString("handleLongPress:")
        )
        mapView.addGestureRecognizer(longPress)
        // Center map initially on first vertex (or skip)
        if (nativeInitial.isNotEmpty()) {
            val region = MKCoordinateRegionForMapRect(nativeInitial.toMKMapRect())
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