package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path

@Composable
fun LocalSongPicker(
    onSongSelected: (MusicSource) -> Unit
) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.let { onSongSelected(MusicSource.Local(it.path)) }
    }

    Button(onClick = { launcher.launch() }) {
        Text("Pick a song file")
    }
}