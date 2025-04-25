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
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Composable
actual fun LocalSongPicker(onSongSelected: (MusicSource) -> Unit) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.nsUrl?.let {
            val localPath = copyToDocuments(it)
            onSongSelected(MusicSource.Local(localPath))
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

@OptIn(ExperimentalForeignApi::class)
fun copyToDocuments(pickedUrl: NSURL): String {
    // 1) Resolve our app's Documents directory
    val docsPath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    val destUrl = NSURL.fileURLWithPath(docsPath)
        .URLByAppendingPathComponent(pickedUrl.lastPathComponent!!)

    // 2) Overwrite if the file name already exists
    val fm = NSFileManager.defaultManager
    if (fm.fileExistsAtPath(destUrl?.path!!)) {
        fm.removeItemAtURL(destUrl, null)
    }

    // 3) Copy (this preserves extended attributes and works with iCloud files)
    fm.copyItemAtURL(pickedUrl, destUrl, null)

    return destUrl.path!!
}