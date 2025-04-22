package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path

@Composable
actual fun LocalSongPicker(onSongSelected: (MusicSource) -> Unit) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.let { onSongSelected(MusicSource.Local(it.path)) }
    }

    IconButton(onClick = { launcher.launch() }) {
        Icon(
            Icons.Default.AudioFile,
            "Pick a local song",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}