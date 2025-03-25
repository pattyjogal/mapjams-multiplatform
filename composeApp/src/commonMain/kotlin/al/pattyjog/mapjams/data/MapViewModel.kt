package al.pattyjog.mapjams.data

import al.pattyjog.mapjams.geo.Map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: MapRepository) : ViewModel() {
    private val _maps = MutableStateFlow<List<Map>>(emptyList())
    val maps: StateFlow<List<Map>> get() = _maps

    init {
        loadMaps()
    }

    private fun loadMaps() {
        viewModelScope.launch {
            _maps.value = repository.getMaps()
        }
    }

    fun addMap(newMap: Map) {
        viewModelScope.launch {
            repository.addMaps(listOf(newMap))
        }
    }

    fun getMapById(id: String): Map? {
        return _maps.value.firstOrNull { it.id == id }
    }
}