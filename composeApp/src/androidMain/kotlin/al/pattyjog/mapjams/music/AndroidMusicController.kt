package al.pattyjog.mapjams.music

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AndroidMusicController(private val context: Context) : MusicController {
    private val exoPlayer = ExoPlayer.Builder(context).build()

    init {
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
    }

    override fun play(musicSource: MusicSource, startAt: Long) {
        when (musicSource) {
            is MusicSource.Local -> {
                val mediaItem = MediaItem.fromUri(musicSource.file)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }

            else -> {
                TODO("Not yet implemented for Spotify/Apple Music")
            }
        }
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun resume() {
        exoPlayer.play()
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override suspend fun fadeOut(durationMs: Long) {
        // Get the current volume (assumed to be between 0 and 1)
        val initialVolume = exoPlayer.volume
        if (initialVolume <= 0f) return  // Nothing to do if already at zero.

        val steps = 20
        val stepDuration = durationMs / steps
        // Compute the volume decrement per step.
        val volumeStep = initialVolume / steps
        // Gradually decrease the volume.
        for (i in 1..steps) {
            withContext(Dispatchers.Main) {
                exoPlayer.volume = initialVolume - i * volumeStep
            }
            delay(stepDuration)
        }
        // Ensure volume is set exactly to 0.
        withContext(Dispatchers.Main) { exoPlayer.volume = 0f }
    }

    override suspend fun fadeIn(durationMs: Long) {
        // Get the current volume.
        val targetVolume = 1.0f
        val initialVolume = exoPlayer.volume
        if (initialVolume >= targetVolume) return  // Already at or above target.

        val steps = 20
        val stepDuration = durationMs / steps
        // Compute the volume increment per step.
        val volumeStep = (targetVolume - initialVolume) / steps
        // Gradually increase the volume.
        for (i in 1..steps) {
            withContext(Dispatchers.Main) {
                exoPlayer.volume = initialVolume + i * volumeStep
            }
            delay(stepDuration)
        }
        // Ensure volume is set exactly to target.
        withContext(Dispatchers.Main) { exoPlayer.volume = targetVolume }
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }
}