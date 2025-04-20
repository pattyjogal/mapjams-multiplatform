package al.pattyjog.mapjams.music

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVKeyValueStatusLoaded
import platform.AVFoundation.AVMetadataCommonKeyArtist
import platform.AVFoundation.AVMetadataCommonKeyTitle
import platform.AVFoundation.AVMetadataFormat
import platform.AVFoundation.AVMetadataFormatID3Metadata
import platform.AVFoundation.AVMetadataItem
import platform.AVFoundation.commonKey
import platform.AVFoundation.commonMetadata
import platform.AVFoundation.loadMetadataForFormat
import platform.AVFoundation.stringValue
import platform.CoreMedia.kCMMetadataFormatType_ID3
import platform.Foundation.NSURL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                    val title = titleItem?.stringValue ?: "Unknown Title"
                    val artist = artistItem?.stringValue ?: "Unknown Artist"

                    cont.resume(Metadata(title, artist))
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
