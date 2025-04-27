package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
actual fun LocalSongPicker(onSongSelected: (MusicSource) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.let {
            try {
                val destination = File(context.filesDir, "${Uuid.random()}.mp3")
                context.contentResolver.openInputStream(it.path.toUri())?.use { inputStream ->
                    destination.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                onSongSelected(MusicSource.Local(destination.path))
        } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Make error clearer
            }
        }
    }

    IconButton(onClick = { launcher.launch() }) {
        Icon(
            Icons.Default.AudioFile,
            "Pick a local song",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}