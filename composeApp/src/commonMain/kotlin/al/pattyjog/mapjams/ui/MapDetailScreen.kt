package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.Metadata
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.music.getMp3Metadata
import al.pattyjog.mapjams.ui.components.DefaultAlbumArt
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
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalResourceApi::class)
@Composable
@Preview
fun MapDetailScreen(
    mapId: String,
    onRegionEdit: (Region) -> Unit,
) {
    val vm = koinViewModel<MapViewModel>()
    val maps by vm.maps.collectAsState()
    val map = maps.firstOrNull { it.id == mapId }

    var regionsWithMetadata: List<Pair<Region, Metadata?>>? by remember { mutableStateOf(null) }

    LaunchedEffect(map) {
        regionsWithMetadata = map?.regions?.map { region ->
            region to region.musicSource?.getMetadata()
        }
    }

    Scaffold(
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
    ) {
        if (regionsWithMetadata != null) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(regionsWithMetadata!!) { (region, metadata) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onRegionEdit(region) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = region.name, style = MaterialTheme.typography.titleLarge)
                            if (metadata != null) {
                                Row {
                                    metadata.artwork?.let { artworkData ->
                                        Image(
                                            bitmap = artworkData.decodeToImageBitmap(),
                                            contentDescription = "Album artwork",
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } ?: DefaultAlbumArt(48.dp)
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
                    }
                }
            }

        } else {
            Text("Map not found")
        }
    }
}