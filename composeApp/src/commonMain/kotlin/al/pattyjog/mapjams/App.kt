package al.pattyjog.mapjams

import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.ui.AppNavigation
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

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

    KoinContext {
        MaterialTheme {
            AppNavigation(maps = sampleMaps)
        }
    }
}