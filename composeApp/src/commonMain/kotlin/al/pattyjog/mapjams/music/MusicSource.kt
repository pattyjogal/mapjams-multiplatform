package al.pattyjog.mapjams.music

sealed class MusicSource {
    abstract fun getMetadata(): Metadata
    data class Local(val file: String) : MusicSource() {
        override fun getMetadata(): Metadata {
            return getMp3Metadata(this)
        }

    }
    data class Spotify(val trackId: String) : MusicSource() {
        override fun getMetadata(): Metadata {
            TODO("Not yet implemented")
        }
    }

    data class AppleMusic(val trackId: String) : MusicSource() {
        override fun getMetadata(): Metadata {
            TODO("Not yet implemented")
        }
    }
}