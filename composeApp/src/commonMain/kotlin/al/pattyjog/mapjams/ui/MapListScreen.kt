package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.Map
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MapListScreen(
    maps: List<Map>,
    onMapClick: (Map) -> Unit
) {
    MaterialTheme {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(maps) { map ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onMapClick(map) }
                ) {
                    Column {
                        Text(text = map.name, style = MaterialTheme.typography.h6)
                        Text(text = "${map.regions.size} regions", style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}