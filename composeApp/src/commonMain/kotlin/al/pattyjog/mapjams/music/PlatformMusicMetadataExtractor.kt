package al.pattyjog.mapjams.music

data class Metadata(
    val title: String = "Unknown Title",
    val artist: String = "Unknown Artist"
)

expect fun getMp3Metadata(musicSource: MusicSource.Local): Metadata