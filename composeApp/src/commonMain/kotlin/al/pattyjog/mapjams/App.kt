package al.pattyjog.mapjams

import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.ui.AppNavigation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import mapjams.composeapp.generated.resources.Res
import mapjams.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    // Sample data for preview
    val sampleMaps = listOf(
        Map(
            id = "1",
            name = "My Map Collection",
            regions = listOf(
                Region(id = "r1", name = "Region 1", polygon = listOf(), musicSource = MusicSource.Local("path/to/song"))
            )
        )
    )

    MaterialTheme {
        AppNavigation(maps = sampleMaps)
    }
}