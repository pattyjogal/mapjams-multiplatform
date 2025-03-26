package al.pattyjog.mapjams.data

import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
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
            loadMaps()
        }
    }

    fun deleteMap(map: Map) {
        viewModelScope.launch {
            repository.deleteMap(map.id)
            loadMaps()
        }
    }

    fun getRegionById(id: String): Region? {
        return _maps.value
            .flatMap { it.regions }
            .firstOrNull { it.id == id }
    }

    fun updateRegion(region: Region) {
        viewModelScope.launch {
            repository.updateRegion(region)
            loadMaps()
        }
    }

    fun getMapById(id: String): Map? {
        return _maps.value.firstOrNull { it.id == id }
    }
}