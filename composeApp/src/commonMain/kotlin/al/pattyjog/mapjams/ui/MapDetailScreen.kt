package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
fun MapDetailScreen(
    mapId: String,
    onRegionEdit: (Region) -> Unit,
) {
    val vm = koinViewModel<MapViewModel>()
    val map = vm.getMapById(mapId)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    map?.let {
                        vm.addRegion(
                            it, Region(
                                id = Uuid.random().toString(),
                                name = "Test Region",
                                polygon = listOf(),
                                musicSource = MusicSource.Local("test")
                            ))
                    }
                }
            ) {
                Icon(Icons.Filled.Add, "Add region")
            }
        }
    ) {
        if (map != null) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(map.regions) { region ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onRegionEdit(region) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(text = region.name, style = MaterialTheme.typography.h6)
                        }
                    }
                }
            }

        } else {
            Text("Map not found")
        }
    }
}