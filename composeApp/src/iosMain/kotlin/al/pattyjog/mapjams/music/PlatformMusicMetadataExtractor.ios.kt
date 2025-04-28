package al.pattyjog.mapjams.music

import bookmarkToUrl
import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVKeyValueStatusLoaded
import platform.AVFoundation.AVMetadataCommonKeyArtist
import platform.AVFoundation.AVMetadataCommonKeyArtwork
import platform.AVFoundation.AVMetadataCommonKeyTitle
import platform.AVFoundation.AVMetadataItem
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.commonKey
import platform.AVFoundation.commonMetadata
import platform.AVFoundation.dataValue
import platform.AVFoundation.stringValue
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSUnderlyingErrorKey
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun getMp3Metadata(musicSource: MusicSource.Local): Metadata? =
    suspendCancellableCoroutine { cont ->
        try {
            val nsurl = bookmarkToUrl(musicSource.file)
            val scoped = nsurl.startAccessingSecurityScopedResource()
            val key = "commonMetadata"

            val opts = mapOf<Any?, Any?>(
                // allows Wi-Fi or cellular
                platform.AVFoundation.AVURLAssetAllowsCellularAccessKey to true
            ) as Map<Any?, *>

            val asset = AVURLAsset.URLAssetWithURL(nsurl, opts)

            asset.loadValuesAsynchronouslyForKeys(listOf(key)) {
                try {
                    memScoped {
                        val errPtr = alloc<ObjCObjectVar<NSError?>>()
                        val status = asset.statusOfValueForKey(key, errPtr.ptr)
                        val nsError = errPtr.value
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
                            val root  = nsError?.userInfo?.get(NSUnderlyingErrorKey) as? NSError
                            Logger.e("AVAsset error domain=${nsError?.domain} code=${nsError?.code}")
                            Logger.e("Underlying error ${root?.localizedDescription}, ${root?.domain}, ${root?.code}")
                            Logger.e("Load failed: ${nsError?.localizedDescription}")
                            cont.resumeWithException(
                                Exception("Failed to load metadata (status=$status): ${nsError?.localizedDescription}, ${musicSource.file}")
                            )
                        }
                    }
                } finally {
                    if (scoped) {
                        nsurl.stopAccessingSecurityScopedResource()
                    }
                }
            }
        } catch (e: Throwable) {
            cont.resumeWithException(e)
        }
    }
