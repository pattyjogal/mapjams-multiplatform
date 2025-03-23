package al.pattyjog.mapjams.music

interface MusicController {
    fun play(musicSource: MusicSource, startAt: Long)
    fun fadeOut()
    fun fadeIn()
    fun pause()
    fun getCurrentPosition() : Long
}