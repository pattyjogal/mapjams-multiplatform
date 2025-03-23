package al.pattyjog.mapjams.music

sealed class MusicSource {
    data class Local(val fileUri: String) : MusicSource()
    data class Spotify(val trackId: String) : MusicSource()
    data class AppleMusic(val trackId: String) : MusicSource()
}