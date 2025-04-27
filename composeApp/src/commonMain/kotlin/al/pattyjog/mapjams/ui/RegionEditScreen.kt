package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.Metadata
import al.pattyjog.mapjams.ui.components.AlbumArt
import al.pattyjog.mapjams.ui.components.EditNameDialog
import al.pattyjog.mapjams.ui.components.LocalSongPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionEditScreen(
    initialRegionId: String,
    onRegionSave: (Region) -> Unit,
    vm: MapViewModel
) {
    val metadata = remember { mutableStateOf<Metadata?>(null) }
    val locationViewModel = koinViewModel<LocationViewModel>()
    val location by locationViewModel.locationFlow.collectAsState()
    val region = vm.getRegionById(initialRegionId)
    val map = vm.getMapForRegion(initialRegionId)

    var shouldShowDialog by remember { mutableStateOf(false) }

    if (region != null) {
        var regionState by remember { mutableStateOf(region) }

        LaunchedEffect(regionState) {
            if (regionState.musicSource != null) {
                metadata.value = regionState.musicSource!!.getMetadata()
            }
        }

        if (shouldShowDialog) {
            EditNameDialog(region.name, onSave = {
                shouldShowDialog = false
                vm.updateRegion(region.copy(name = it))
            }, onDismiss = { shouldShowDialog = false })
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            region.name
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            shouldShowDialog = true
                        }) {
                            Icon(Icons.Rounded.Edit, "Edit map name")
                        }
                    })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        vm.updateRegion(regionState)
                        onRegionSave(regionState)
                    }
                ) {
                    Icon(Icons.Filled.Save, "Save region")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.padding(horizontal = 8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row {
                            AlbumArt(metadata.value)
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

