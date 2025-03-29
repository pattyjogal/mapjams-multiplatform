package al.pattyjog.mapjams.music

interface MusicController {
    fun play(musicSource: MusicSource, startAt: Long = 0L)
    fun pause()
    fun resume()
    fun stop()
    fun fadeOut(durationMs: Long)
    fun fadeIn(durationMs: Long)
    fun getCurrentPosition(): Long
}