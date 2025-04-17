package al.pattyjog.mapjams.music

actual fun getMp3Metadata(musicSource: MusicSource.Local): Metadata {
    return Metadata(title = "Unknown Title", artist = "Unknown Artist")
}