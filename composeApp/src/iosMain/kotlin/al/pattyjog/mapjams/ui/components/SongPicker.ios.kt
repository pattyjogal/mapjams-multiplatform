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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LocalSongPicker(onSongSelected: (MusicSource) -> Unit) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("mp3"))
    ) { file ->
        file?.nsUrl?.let {
            val bookmark = it.toSecurityBookmark()
            onSongSelected(MusicSource.Local(bookmark.base64Encoding()))
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
    Logger.d("Is placeholder: ${isPlaceholder(pickedUrl)}")
    Logger.d("Is readable: ${ensureReadable(pickedUrl)}")
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

@OptIn(ExperimentalForeignApi::class)
fun isPlaceholder(url: NSURL): Boolean {
    val values = url.resourceValuesForKeys(
        listOf(
            NSURLUbiquitousItemIsDownloadedKey,     // true = local
            NSURLIsUbiquitousItemKey                // true = lives in iCloud
        ), error = null
    )
    val isUbiquitous = values?.get(NSURLIsUbiquitousItemKey) as? Boolean ?: false
    val downloaded   = values?.get(NSURLUbiquitousItemIsDownloadedKey) as? Boolean ?: true
    return isUbiquitous && !downloaded
}

@OptIn(ExperimentalForeignApi::class)
fun ensureReadable(url: NSURL): Boolean {
    // 1 ⟶ open security scope (returns FALSE if not allowed)
    val scoped = url.startAccessingSecurityScopedResource()

    // 2 ⟶ if the item lives in iCloud Drive, download it
    val values = url.resourceValuesForKeys(
        listOf(NSURLIsUbiquitousItemKey, NSURLUbiquitousItemIsDownloadedKey),
        null
    )

    val needsDownload = (values?.get(NSURLIsUbiquitousItemKey) as? Boolean == true) &&
            (values.get(NSURLUbiquitousItemIsDownloadedKey) as? Boolean == false)

    if (needsDownload) {
        NSFileManager.defaultManager.startDownloadingUbiquitousItemAtURL(url, null)
        // ...observe KVO NSURLUbiquitousItemIsDownloadedKey until it becomes true...
    }

    return scoped || !needsDownload
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun NSURL.toSecurityBookmark(): NSData {
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

        return data
    }
}