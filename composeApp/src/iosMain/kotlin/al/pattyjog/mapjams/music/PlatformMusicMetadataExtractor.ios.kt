package al.pattyjog.mapjams.music

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.Image
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVKeyValueStatusLoaded
import platform.AVFoundation.AVMetadataCommonKeyArtist
import platform.AVFoundation.AVMetadataCommonKeyArtwork
import platform.AVFoundation.AVMetadataCommonKeyTitle
import platform.AVFoundation.AVMetadataItem
import platform.AVFoundation.commonKey
import platform.AVFoundation.commonMetadata
import platform.AVFoundation.dataValue
import platform.AVFoundation.stringValue
import platform.Foundation.NSURL
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getMp3Metadata(musicSource: MusicSource.Local): Metadata? =
    suspendCancellableCoroutine { cont ->
        try {
            val nsurl = NSURL.fileURLWithPath(musicSource.file)
            val asset = AVAsset.assetWithURL(nsurl)
            val key = "commonMetadata"

            asset.loadValuesAsynchronouslyForKeys(listOf(key)) {
                val status = asset.statusOfValueForKey(key, null)
                if (status == AVKeyValueStatusLoaded) {
                    @Suppress("UNCHECKED_CAST") val items =
                        asset.commonMetadata as List<AVMetadataItem>
                    val titleItem = items.firstOrNull {
                        it.commonKey == AVMetadataCommonKeyTitle
                    }
                    val artistItem = items.firstOrNull {
                        it.commonKey == AVMetadataCommonKeyArtist
                    }
                    val albumArt = items.firstOrNull {
                        it.commonKey == AVMetadataCommonKeyArtwork
                    }
                    val title = titleItem?.stringValue ?: "Unknown Title"
                    val artist = artistItem?.stringValue ?: "Unknown Artist"
                    val artwork = albumArt?.dataValue?.let { data ->
                        data.bytes?.let { bytesPtr ->
                            ByteArray(data.length.toInt()).also { array ->
                                memScoped {
                                    memcpy(array.refTo(0), bytesPtr, data.length)
                                }
                            }
                        }
                    }

                    cont.resume(Metadata(title, artist, artwork))
                } else {
                    cont.resumeWithException(
                        Exception("Failed to load metadata (status=$status), ${musicSource.file}")
                    )
                }
            }
        } catch (e: Throwable) {
            cont.resumeWithException(e)
        }
    }
