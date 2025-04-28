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
import platform.MapKit.addOverlays
import platform.MapKit.removeOverlay
import platform.MapKit.removeOverlays
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UILongPressGestureRecognizer
import platform.UIKit.systemBlueColor
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class MapDelegate(
    private val mapView: MKMapView,
    private val onPolygonUpdate: (List<LatLng>) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {
    private var userPolygon: MKPolygon? = null
    private var currentOther: List<MKPolygon> = emptyList()
    private val _nativePolygon = mutableStateListOf<CValue<CLLocationCoordinate2D>>()
    val nativePolygon: List<CValue<CLLocationCoordinate2D>> get() = _nativePolygon


    private var hasCentered = false

    fun setInitial(native: List<CValue<CLLocationCoordinate2D>>) {
        _nativePolygon.clear()
        _nativePolygon += native
        redrawPins()
        refreshUserPolygon()
    }

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    @ObjCAction
    fun handleLongPress(gesture: UILongPressGestureRecognizer) {
        if (gesture.state == UIGestureRecognizerStateBegan) {
            val pt = gesture.locationInView(mapView)
            val coord = mapView.convertPoint(pt, toCoordinateFromView = mapView)
            _nativePolygon += coord
            onPolygonUpdate(_nativePolygon.map {
                it.useContents {
                    LatLng(
                        latitude,
                        longitude
                    )
                }
            })
            redrawPins()
            refreshUserPolygon()
        }
    }

    var otherOverlays: List<MKPolygon> = emptyList()
        set(value) {
            field = value
            refreshOtherOverlays(value)
            refreshUserPolygon()
        }

    private fun refreshUserPolygon() {
        val newPoly = nativePolygon.toMKPolygon() ?: return
        val old = userPolygon

        dispatch_async(dispatch_get_main_queue()) {
            old?.let { mapView.removeOverlay(it) }
            mapView.addOverlay(newPoly)
            userPolygon = newPoly                   // remember for next time
        }
    }

    private fun refreshOtherOverlays(newList: List<MKPolygon>) {
        /* nothing to do if the set is unchanged */
        if (newList === currentOther) return

        val toRemove = currentOther
        currentOther = newList            // update snapshot

        dispatch_async(dispatch_get_main_queue()) {
            if (toRemove.isNotEmpty()) mapView.removeOverlays(toRemove)
            if (newList.isNotEmpty()) mapView.addOverlays(newList)
        }
    }

    private fun redrawPins() {
        mapView.removeAnnotations(mapView.annotations)
        nativePolygon.forEachIndexed { idx, c ->
            mapView.addAnnotation(VertexAnnotation(idx, c))
        }
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
        if (annotationView.annotation is VertexAnnotation &&
            (didChangeDragState == MKAnnotationViewDragStateEnding ||
                    didChangeDragState == MKAnnotationViewDragStateCanceling)
        ) {
            val v = annotationView.annotation as VertexAnnotation
            _nativePolygon[v.vertexIndex] = v.coordinate

            onPolygonUpdate(_nativePolygon.map { it.useContents { LatLng(latitude, longitude) } })
            refreshUserPolygon()                               // 🔄 instead of redrawOverlays()
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
    isLocked: Boolean
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

    LaunchedEffect(isLocked) {
        mapView.apply {
            scrollEnabled = !isLocked
            zoomEnabled = !isLocked
            pitchEnabled = !isLocked
            rotateEnabled = !isLocked
        }
    }

    // Whenever otherRegions changes, update delegate
    LaunchedEffect(nativeOthers) {
        delegate.otherOverlays = nativeOthers
    }

    // Host the MKMapView inside Compose on iOS
    UIKitView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}