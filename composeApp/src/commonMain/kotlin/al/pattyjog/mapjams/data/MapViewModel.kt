package al.pattyjog.mapjams.data

import al.pattyjog.mapjams.geo.ActiveMapHolder
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MapViewModel(private val repository: MapRepository, private val activeMapFlow: MutableStateFlow<Map?>) : ViewModel() {
    private val _maps = MutableStateFlow<List<Map>>(emptyList())
    val maps: StateFlow<List<Map>> get() = _maps

    init {
        loadMaps()
    }

    private fun loadMaps() {
        viewModelScope.launch {
            Logger.d { "Loading maps" }
            val ref = repository.getMaps()
            _maps.value = ref
            activeMapFlow.value = repository.getMaps().firstOrNull()
            Logger.d { "Loaded maps" }
        }
    }

    fun addMap(newMap: Map) {
        viewModelScope.launch {
            repository.addMaps(listOf(newMap))
            loadMaps()
        }
    }

    fun addRegion(map: Map, newRegion: Region) {
        viewModelScope.launch {
            repository.addRegionToMap(map, newRegion)
            loadMaps()
        }
    }

    fun deleteMap(map: Map) {
        viewModelScope.launch {
            repository.deleteMap(map.id)
            loadMaps()
        }
    }

    fun updateMap(map: Map) {
        viewModelScope.launch {
            repository.updateMap(map)
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
            Logger.v { "Updating region: $region" }
            repository.updateRegion(region)
            loadMaps()
        }
    }

    fun deleteRegion(region: Region) {
        viewModelScope.launch {
            repository.deleteRegion(region.id)
            loadMaps()
        }
    }

    fun getMapForRegion(regionId: String): Map? {
        return _maps.value.firstOrNull { map -> map.regions.any { it.id == regionId } }
    }

    fun getMapById(id: String): Map? {
        return _maps.value.firstOrNull { it.id == id }
    }
}