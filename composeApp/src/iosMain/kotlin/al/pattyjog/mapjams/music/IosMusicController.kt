package al.pattyjog.mapjams.music

import al.pattyjog.mapjams.bookmarkToUrl
import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL
import platform.Foundation.addObserver
import kotlin.math.max
import kotlin.math.min
import kotlinx.cinterop.*
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSNumber
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import kvo.NSKeyValueObservingProtocol
import platform.Foundation.NSError

class IosMusicController(private val isPlayingFlow: MutableStateFlow<Boolean>) : MusicController {
    private var player: AVAudioPlayer? = null
    private val observer = Observer()
    private var url: NSURL? = null

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun play(musicSource: MusicSource, startAt: Long) {
        when (musicSource) {
            is MusicSource.Local -> {
                memScoped {
                    Logger.v { "Stopping" }
                    player?.stop()
                    player = null
                    Logger.v { "Stopped" }
                    val errPtr = alloc<ObjCObjectVar<NSError?>>()
                    url = bookmarkToUrl(musicSource.file)
                    url?.startAccessingSecurityScopedResource()
                    Logger.v { "Got music source: $url" }
                    val isReachable = url?.checkResourceIsReachableAndReturnError(errPtr.ptr) ?: false
                    if (!isReachable) {
                        Logger.e { "URL is not reachable: ${errPtr.value?.localizedDescription}" }
                        Logger.e { "File should have been at ${musicSource.file}" }
                    }
                    val newPlayer = try {
                        AVAudioPlayer(url!!, error = errPtr.ptr)
                    } catch (t: Throwable) {
                        val desc = errPtr.value?.localizedDescription ?: "unknown error"
                        Logger.e(t) { "AVAudioPlayer returned null: $desc" }
                        return
                    }
                    Logger.v { "Here's the player: $newPlayer" }
                    newPlayer.apply {
                        numberOfLoops = -1
                        volume = 1.0f
                        prepareToPlay()
                        currentTime = startAt.toDouble() / 1000
                        Logger.v { "Adding observer" }
                        addObserver(
                            observer = observer,
                            forKeyPath = "rate",
                            options = NSKeyValueObservingOptionNew,
                            context = null
                        )
                        Logger.v { "Added observer" }
                        delegate = object : NSObject(), AVAudioPlayerDelegateProtocol {
                            override fun audioPlayerDidFinishPlaying(
                                player: AVAudioPlayer,
                                successfully: Boolean
                            ) = push(false)
                        }
                        play()
                        push(true)
                    }
                    player = newPlayer
                }
            }

            else -> {
                TODO("Not yet implemented for Spotify/Apple Music")
            }
        }
    }

    override fun pause() {
        player?.pause()
        push(false)
    }

    override fun resume() {
        player?.play()
        push(true)
    }

    override fun stop() {
        player?.stop()
        release()
        push(false)
        player = null
    }

    override suspend fun fadeOut(durationMs: Long) {
        val p = player ?: return
        val initial = p.volume
        if (initial <= 0f) return

        val steps = 20
        val stepDur = durationMs / steps
        val volStep = initial / steps

        for (i in 1..steps) {
            // update on main thread
            withContext(Dispatchers.Main) {
                p.volume = max(initial - i * volStep, 0f)
            }
            delay(stepDur)
        }
        withContext(Dispatchers.Main) {
            p.volume = 0f
        }
    }

    override suspend fun fadeIn(durationMs: Long) {
        val p = player ?: return
        val target = 1.0f
        val initial = p.volume
        if (initial >= target) return

        val steps = 20
        val stepDur = durationMs / steps
        val volStep = (target - initial) / steps

        for (i in 1..steps) {
            withContext(Dispatchers.Main) {
                p.volume = min(initial + i * volStep, target)
            }
            delay(stepDur)
        }
        withContext(Dispatchers.Main) {
            p.volume = target
        }
    }

    override fun getCurrentPosition(): Long {
        return ((player?.currentTime ?: 0.0) * 1000.0).toLong()
    }

    private fun push(isPlaying: Boolean) {
        isPlayingFlow.value = isPlaying
    }

    // This doesn't do anything
    @OptIn(ExperimentalForeignApi::class)
    private inner class Observer : NSObject(), NSKeyValueObservingProtocol {
        @OptIn(ExperimentalForeignApi::class)
        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: CPointer<*>?
        ) {
            Logger.v { "I'm in observeValueForKeyPath" }
            if (keyPath == "rate" && ofObject === player) {
                val newRate =
                    (change?.get(NSKeyValueChangeNewKey) as? NSNumber)?.doubleValue ?: 0.0
                push(newRate > 0.0)   // update the StateFlow
            }
            Logger.v { "Finished observeValueForKeyPath" }

        }
    }

    private fun release() {
        player?.removeObserver(observer, "rate")
        player?.delegate = null
        url?.stopAccessingSecurityScopedResource()
    }
}