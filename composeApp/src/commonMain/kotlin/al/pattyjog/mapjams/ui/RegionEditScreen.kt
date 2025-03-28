package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.ui.components.LocalSongPicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegionEditScreen(
    initialRegionId: String,
    onRegionSave: (Region) -> Unit
) {
    val vm = koinViewModel<MapViewModel>()
    val locationViewModel = koinViewModel<LocationViewModel>()
    val location by locationViewModel.locationFlow.collectAsState()
    val region = vm.getRegionById(initialRegionId)
    if (region != null) {
        var regionState by remember { mutableStateOf(region) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        vm.updateRegion(regionState)
                        onRegionSave(regionState)
                    }
                ) {
                    Icon(Icons.Filled.Check, "Save region")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TextField(value = regionState.name, onValueChange = { regionState = regionState.copy(name = it) })
                Row {
                    Text(regionState.musicSource?.getMetadata()?.let {"${it.title} by ${it.artist}"} ?: "No song picked")
                    LocalSongPicker(onSongSelected = { newSong ->
                        regionState = regionState.copy(musicSource = newSong)
                    })
                }
                PlatformMapRegionDrawingComponent(
                    initialPolygon = region.polygon,
                    onPolygonUpdate = {
                        regionState = regionState.copy(polygon = it)
                    }
                )
            }
        }
    } else {
        Text("Region not found")
    }
}