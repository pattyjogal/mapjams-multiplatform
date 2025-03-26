package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.ui.components.LocalSongPicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
    val region = vm.getRegionById(initialRegionId)
    if (region != null) {
        var regionState by remember { mutableStateOf(region) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onRegionSave(regionState)
                    }
                ) {
                    Icon(Icons.Filled.Check, "Save region")
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LocalSongPicker(onSongSelected = { newSong ->
                    regionState = region.copy(musicSource = newSong)
                })
                PlatformMapRegionDrawingComponent()
            }

        }
    } else {
        Text("Region not found")
    }
}