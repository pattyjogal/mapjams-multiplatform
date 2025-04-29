package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.GeoJson
import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.decodeJson
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MapListScreen(
    onMapClick: (Map) -> Unit,
    vm: MapViewModel
) {
    val maps by vm.maps.collectAsState()
    val activeMapFlow = koinInject<MutableStateFlow<Map?>>(named("activeMapFlow"))
    val activeMap by activeMapFlow.collectAsState()

    val composableScope = rememberCoroutineScope()

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("geojson"))
    ) { file ->
        composableScope.launch {
            try {
                val json = file?.readString() ?: throw Exception("No file selected")
                val geoJson = decodeJson<GeoJson>(json)
                val newMap = Map(Uuid.random().toString(), "New Map", emptyList())
                vm.addMap(newMap)
                geoJson.features.forEach {
                    vm.addRegion(
                        newMap, Region(
                            Uuid.random().toString(),
                            name = it.properties.name ?: "New Region",
                            polygon = it.geometry.coordinates[0].map { (lng, lat) ->
                                LatLng(
                                    lat,
                                    lng
                                )
                            },
                            musicSource = null,
                        )
                    )
                }

            } catch (t: Throwable) {
                Logger.e(t) { "Error importing GeoJson" }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.addMap(Map(Uuid.random().toString(), "New Map", emptyList())) },
                icon = { Icon(Icons.Filled.Add, "Create a new map") },
                text = { Text("New Map") }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Your Maps") },
                actions = {
                    IconButton(onClick = {
                        launcher.launch()
                    }) {
                        Icon(Icons.Rounded.FileOpen, "Import from GeoJson")
                    }
                })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(maps) { map ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onMapClick(map) }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Row {
                            RadioButton(selected = activeMap?.id == map.id, onClick = {
                                activeMapFlow.value = map
                            })
                            Column {
                                Text(text = map.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${map.regions.size} regions",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        IconButton(
                            onClick = { vm.deleteMap(map) },
                        ) {
                            Icon(Icons.Filled.Delete, "Delete this map")
                        }
                    }
                }
            }
        }

    }
}