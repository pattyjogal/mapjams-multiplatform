package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.MusicSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSURLBookmarkCreationWithSecurityScope
import platform.Foundation.NSURLIsUbiquitousItemKey
import platform.Foundation.NSURLUbiquitousItemIsDownloadedKey
import platform.Foundation.NSUserDomainMask
import platform.Foundation.base64Encoding

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun LocalSongPicker(onSongSelected: (MusicSource) -> Unit) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.nsUrl?.let { pickedUrl ->
            val accessGranted = pickedUrl.startAccessingSecurityScopedResource()
            if (accessGranted) {
                Logger.i { "Security scope access granted for: $pickedUrl" }
                val isReachable = memScoped {
                    val errPtr = alloc<ObjCObjectVar<NSError?>>()
                    pickedUrl.checkResourceIsReachableAndReturnError(errPtr.ptr).also {
                        if (!it && errPtr.value != null) {
                            Logger.w { "checkResourceIsReachable failed: ${errPtr.value?.localizedDescription}" }
                        }
                    }
                }

                if (isReachable) {
                    try {
                        val bookmark: NSData =
                            pickedUrl.toSecurityBookmark()
                        onSongSelected(MusicSource.Local(bookmark.base64Encoding()))
                        Logger.i { "Successfully created and encoded bookmark." }

                    } catch (e: Exception) {
                        Logger.e(e) { "Error during bookmark creation/encoding." }
                    }
                } else {
                    Logger.e { "Picked URL is not reachable even after starting access." }
                }
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

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun NSURL.toSecurityBookmark(): NSData {
    Logger.d { "Creating bookmark for $this" }
    memScoped {
        val errPtr = alloc<ObjCObjectVar<NSError?>>()
        val data = this@toSecurityBookmark.bookmarkDataWithOptions(
            options = NSURLBookmarkCreationWithSecurityScope,   // ← main flag
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = errPtr.ptr
        )
        requireNotNull(data) {
            "Bookmark creation failed: ${errPtr.value?.localizedDescription}"
        }
        val error = errPtr.value
        if (error != null) {
            Logger.w { "Bookmark creation produced an error (but data was returned): ${error.localizedDescription}" }
        }

        return data
    }
}