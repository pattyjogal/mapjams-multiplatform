package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.Metadata
import al.pattyjog.mapjams.ui.components.LocalSongPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RegionEditScreen(
    initialRegionId: String,
    onRegionSave: (Region) -> Unit
) {
    val vm = koinViewModel<MapViewModel>()
    val metadata = remember { mutableStateOf<Metadata?>(null) }
    val locationViewModel = koinViewModel<LocationViewModel>()
    val location by locationViewModel.locationFlow.collectAsState()
    val region = vm.getRegionById(initialRegionId)
    val map = vm.getMapForRegion(initialRegionId)
    if (region != null) {
        var regionState by remember { mutableStateOf(region) }

        LaunchedEffect(regionState) {
            if (regionState.musicSource != null) {
                metadata.value = regionState.musicSource!!.getMetadata()
            }
        }

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
                Column(Modifier.padding(horizontal = 8.dp)) {
                    TextField(value = regionState.name, onValueChange = { regionState = regionState.copy(name = it) })
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row {
                            metadata.value?.artwork?.let { artworkData ->
                                Image(
                                    bitmap = artworkData.decodeToImageBitmap(),
                                    contentDescription = "",
                                    modifier = Modifier.size(96.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                metadata.value?.let {
                                    Text(it.title, style = MaterialTheme.typography.titleMedium)
                                    Text(it.artist, style = MaterialTheme.typography.titleSmall)
                                } ?: Text("Cannot parse metadata")
                            }

                        }
                        LocalSongPicker(onSongSelected = { newSong ->
                            regionState = regionState.copy(musicSource = newSong)
                        })
                    }
                }
                PlatformMapRegionDrawingComponent(
                    initialPolygon = region.polygon,
                    onPolygonUpdate = {
                        regionState = regionState.copy(polygon = it)
                    },
                    otherRegions = map?.regions?.filter { it.id != initialRegionId } ?: emptyList()
                )
            }
        }
    } else {
        Text("Region not found")
    }
}