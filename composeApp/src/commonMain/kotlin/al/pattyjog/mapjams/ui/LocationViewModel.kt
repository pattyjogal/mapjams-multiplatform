package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LocationViewModel(
    private val _locationFlow: MutableStateFlow<LatLng?>,
    private val _regionFlow: MutableStateFlow<Region?>,
    private val _activeMapFlow: MutableStateFlow<Map?>,
    private val musicController: MusicController
) : ViewModel() {
    val locationFlow = _locationFlow.asStateFlow()
    val regionFlow = _regionFlow.asStateFlow()
    val activeMapFlow = _activeMapFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _locationFlow,
                _activeMapFlow
            ) { location, activeMap ->
                val activeRegions = activeMap?.regions ?: emptyList()
                activeRegions.firstOrNull { region ->
                    location?.let { isPointInPolygon(it, region.polygon) } == true
                }
            }
                .collect { region ->
                    musicController.stop()
                    _regionFlow.value = region
                    region?.musicSource?.let { musicController.play(it, 0) }
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