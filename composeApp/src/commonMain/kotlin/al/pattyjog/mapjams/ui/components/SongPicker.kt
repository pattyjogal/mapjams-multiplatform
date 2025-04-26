package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.runtime.Composable

@Composable
expect fun LocalSongPicker(
    onSongSelected: (MusicSource) -> Unit
)