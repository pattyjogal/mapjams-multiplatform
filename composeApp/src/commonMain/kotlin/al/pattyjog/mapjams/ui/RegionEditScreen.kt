package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.ui.components.LocalSongPicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun RegionEditScreen(
    initialRegion: Region,
    onRegionSave: (Region) -> Unit
) {
    var region by remember { mutableStateOf(initialRegion) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onRegionSave(region)
                }
            ) {
                Icon(Icons.Filled.Check, "Save region")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LocalSongPicker(onSongSelected = { newSong ->
                region = region.copy(musicSource = newSong)
            })
            PlatformMapRegionDrawingComponent()
        }

    }
}