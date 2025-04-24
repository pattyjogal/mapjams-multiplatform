package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Map
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
fun MapListScreen(
    onMapClick: (Map) -> Unit,
    vm: MapViewModel
) {
    val maps by vm.maps.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.addMap(Map(Uuid.random().toString(), "New Map", emptyList())) },
                icon = { Icon(Icons.Filled.Add, "Create a new map") },
                text = { Text("New Map") }
            )
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(maps) { map ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onMapClick(map) }
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Column {
                            Text(text = map.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${map.regions.size} regions", style = MaterialTheme.typography.titleSmall)
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