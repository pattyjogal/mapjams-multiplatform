package al.pattyjog.mapjams.music

class IosMusicController : MusicController {
    override fun play(musicSource: MusicSource, startAt: Long) {

    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun stop() {
    }

    override suspend fun fadeOut(durationMs: Long) {
    }

    override suspend fun fadeIn(durationMs: Long) {
    }

    override fun getCurrentPosition(): Long {
        return 0
    }
}