package al.pattyjog.mapjams.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RegionEditScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        PlatformMapRegionDrawingComponent()
    }
}