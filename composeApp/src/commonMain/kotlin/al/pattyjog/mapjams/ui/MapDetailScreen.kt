package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.Metadata
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.music.getMp3Metadata
import al.pattyjog.mapjams.ui.components.AlbumArt
import al.pattyjog.mapjams.ui.components.DefaultAlbumArt
import al.pattyjog.mapjams.ui.components.EditNameDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MapDetailScreen(
    mapId: String,
    onRegionEdit: (Region) -> Unit,
    vm: MapViewModel
) {
    val maps by vm.maps.collectAsStateWithLifecycle()

    var shouldShowDialog by remember { mutableStateOf(false) }

    LaunchedEffect(maps) {
        Logger.v { "Maps: ${maps.hashCode()}" }
    }

    val map = maps.firstOrNull { it.id == mapId }
    val regionsWithMetadata by produceState<List<Pair<Region, Metadata?>>>(
        initialValue = emptyList(),
        map
    ) {
        value = map?.regions?.map { region ->
            val metadata = region.musicSource?.getMetadata()
            region to metadata
        } ?: emptyList()
    }

    if (shouldShowDialog) {
        EditNameDialog(map?.name ?: "", onSave = {
            shouldShowDialog = false
            vm.updateMap(map!!.copy(name = it))
        }, onDismiss = { shouldShowDialog = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        map?.name ?: "Loading..."
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
                    map?.let {
                        vm.addRegion(
                            it, Region(
                                id = Uuid.random().toString(),
                                name = "New Region",
                                polygon = listOf(),
                                musicSource = null,
                            )
                        )
                    }
                }
            ) {
                Icon(Icons.Filled.Add, "Add region")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(regionsWithMetadata, key = { it.first.id }) { (region, metadata) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onRegionEdit(region) }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = region.name, style = MaterialTheme.typography.titleLarge)
                            if (metadata != null) {
                                Row {
                                    AlbumArt(metadata, size = 48.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            metadata.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            metadata.artist,
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                    }
                                }
                            } else if (region.musicSource == null) {
                                Text("No song selected")
                            } else {
                                Text("...")
                            }
                        }
                        IconButton(onClick = {
                            vm.deleteRegion(region)
                        }) {
                            Icon(Icons.Rounded.Delete, "Delete this region")
                        }
                    }
                }
            }
        }
    }
}