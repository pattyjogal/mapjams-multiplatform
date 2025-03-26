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

    KoinContext {
        MaterialTheme {
            AppNavigation()
        }
    }
}