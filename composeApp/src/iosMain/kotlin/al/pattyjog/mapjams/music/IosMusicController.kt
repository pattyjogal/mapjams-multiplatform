package al.pattyjog.mapjams.music

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

class IosMusicController(private val isPlayingFlow: MutableStateFlow<Boolean>) : MusicController {
    private var player: AVAudioPlayer? = null
    private val observer = Observer()

    @OptIn(ExperimentalForeignApi::class)
    override fun play(musicSource: MusicSource, startAt: Long) {
        when (musicSource) {
            is MusicSource.Local -> {
                player?.stop()
                player = null

                val url = NSURL.fileURLWithPath(musicSource.file)
                val newPlayer = AVAudioPlayer(url, error = null)
                newPlayer.apply {
                    numberOfLoops = -1
                    volume = 1.0f
                    prepareToPlay()
                    currentTime = startAt.toDouble() / 1000
                    addObserver(
                        observer = observer,
                        forKeyPath = "rate",
                        options = NSKeyValueObservingOptionNew,
                        context = null
                    )
                    delegate = object : NSObject(), AVAudioPlayerDelegateProtocol {
                        override fun audioPlayerDidFinishPlaying(
                            player: AVAudioPlayer,
                            successfully: Boolean
                        ) = push(false)
                    }
                    play()
                }
                player = newPlayer
            }

            else -> {
                TODO("Not yet implemented for Spotify/Apple Music")
            }
        }
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        player?.play()
    }

    override fun stop() {
        player?.stop()
        release()
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

    @OptIn(ExperimentalForeignApi::class)
    private inner class Observer : NSObject(), NSKeyValueObservingProtocol {
        @OptIn(ExperimentalForeignApi::class)
        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: CPointer<*>?
        ) {
            if (keyPath == "rate" && ofObject === player) {
                val newRate =
                    (change?.get(NSKeyValueChangeNewKey) as? NSNumber)?.doubleValue ?: 0.0
                push(newRate > 0.0)   // update the StateFlow
            }
        }
    }

    private fun release() {
        player?.removeObserver(observer, "rate")
        player?.delegate = null
    }
}