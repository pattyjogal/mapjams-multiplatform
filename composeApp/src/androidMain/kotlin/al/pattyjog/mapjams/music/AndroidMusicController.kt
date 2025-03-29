package al.pattyjog.mapjams.music

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AndroidMusicController(private val context: Context) : MusicController {
    private val exoPlayer = ExoPlayer.Builder(context).build()
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

    override fun fadeOut(durationMs: Long) {
        TODO("Not yet implemented")
    }

    override fun fadeIn(durationMs: Long) {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }
}