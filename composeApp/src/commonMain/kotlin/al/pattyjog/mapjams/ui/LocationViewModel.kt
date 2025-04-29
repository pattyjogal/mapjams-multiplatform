package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.LocationUpdate
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModel(
    private val _rawLocationFlow: MutableSharedFlow<LocationUpdate?>,
    private val _locationFlow: MutableStateFlow<LatLng?>,
    private val _regionFlow: MutableStateFlow<Region?>,
    private val _activeMapFlow: MutableStateFlow<Map?>,
    private val musicController: MusicController
) : ViewModel() {
    val rawLocationFlow = _rawLocationFlow.asSharedFlow()
    val locationFlow = _locationFlow.asStateFlow()
    val regionFlow = _regionFlow.asStateFlow()
    val activeMapFlow = _activeMapFlow.asStateFlow()

    private val window = ArrayDeque<LocationUpdate>()

    init {
        viewModelScope.launch {
            _rawLocationFlow.collect { location ->
                location?.let { window += it }
                while (location?.run { window.first().elapsedRealtimeNanos <  elapsedRealtimeNanos - 10_000_000_000 } == true) {
                    window.removeFirst()
                }

                if (location?.run { accuracy <= 15f } == true) {
                    _locationFlow.value = smoothWithWeightedAverage(window)
                }
            }
        }
        viewModelScope.launch {
            combine(
                _locationFlow,
                _activeMapFlow
            ) { location, activeMap ->
                Logger.v { "Location: $location" }
                val activeRegions = activeMap?.regions ?: emptyList()
                activeRegions.firstOrNull { region ->
                    location?.let { isPointInPolygon(it, region.polygon) } == true
                }.also { Logger.v("Active region: ${it.hashCode()}") }
            }
                .distinctUntilChanged()
                .drop(1)
                // TODO: Still have the bug where going from A to B to A quickly fades to zero
                .flatMapLatest {
                    flow {
                        val fadeOutJob = launch { musicController.fadeOut(5000) }
                        try {
                            Logger.v { "Starting fade out, will fade out in 5 seconds" }
                            // Wait for 5 seconds.
                            delay(5000)
                            Logger.v { "Fade out complete, emitting region" }
                            // If still in the same region after 5 seconds, emit the region.
                            emit(it)
                        } catch (e: CancellationException) {
                            Logger.v(e) { "Fade out cancelled" }
                            // If the flow is cancelled (i.e. the region changed during the delay),
                            // execute fadeIn to reverse the fadeOut.
                            withContext(NonCancellable) {
                                musicController.fadeIn(500)
                            }
                            throw e // Propagate the cancellation.
                        } finally {
                            Logger.v { "Fade out finally" }
                            // Ensure the fadeOut job is cancelled if it's still running.
                            fadeOutJob.cancel()
                        }
                    }
                }
                .collect { region ->
                    Logger.d { "Region change: $region" }
                    if (_regionFlow.value != region) {
                        musicController.stop()
                        _regionFlow.value = region
                        region?.musicSource?.let {
                            musicController.play(it, 0)
                            musicController.fadeIn(2_000)
                        }
                    }
                }
        }
    }

    private fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var intersects = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            // For clarity, extract the coordinates.
            val xi = polygon[i].latitude
            val yi = polygon[i].longitude
            val xj = polygon[j].latitude
            val yj = polygon[j].longitude
            // Check if the point is within the horizontal bounds of the edge.
            val condition = ((yi > point.longitude) != (yj > point.longitude)) &&
                    (point.latitude < (xj - xi) * (point.longitude - yi) / (yj - yi) + xi)
            if (condition) {
                intersects = !intersects
            }
            j = i
        }
        return intersects
    }
}

private fun smoothWithWeightedAverage(window: List<LocationUpdate>): LatLng? {
    if (window.isEmpty()) {
        return null
    }

    var totalWeight = 0.0
    var weightedLatSum = 0.0
    var weightedLngSum = 0.0
    // Use a small epsilon to prevent division by zero and give a max weight
    val minAccuracy = 0.1f

    window.forEach { update ->
        // Weight inversely proportional to accuracy (higher accuracy = smaller number = higher weight)
        // Clamp accuracy to avoid extremely high weights or division by zero
        val weight = 1.0 / max(update.accuracy, minAccuracy).toDouble()

        weightedLatSum += update.latitude * weight
        weightedLngSum += update.longitude * weight
        totalWeight += weight
    }

    if (totalWeight == 0.0) {
        // Fallback if all weights end up being zero (highly unlikely with clamping)
        // Could return the latest raw location or null
        return window.lastOrNull()?.let { LatLng(it.latitude, it.longitude) }
    }

    val smoothedLat = weightedLatSum / totalWeight
    val smoothedLng = weightedLngSum / totalWeight

    return LatLng(smoothedLat, smoothedLng)
}
