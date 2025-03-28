package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.LatLng
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class LocationViewModel(
    locationFlow: MutableStateFlow<LatLng?>
) : ViewModel() {
    val currentLocation: MutableStateFlow<LatLng?> = locationFlow
}