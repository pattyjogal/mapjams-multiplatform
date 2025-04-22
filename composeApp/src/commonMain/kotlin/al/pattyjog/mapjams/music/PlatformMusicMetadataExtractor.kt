package al.pattyjog.mapjams.music

import androidx.compose.ui.graphics.ImageBitmap

data class Metadata(
    val title: String = "Unknown Title",
    val artist: String = "Unknown Artist",
    val artwork: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Metadata

        if (title != other.title) return false
        if (artist != other.artist) return false
        if (artwork != null) {
            if (other.artwork == null) return false
            if (!artwork.contentEquals(other.artwork)) return false
        } else if (other.artwork != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + (artwork?.contentHashCode() ?: 0)
        return result
    }
}

expect suspend fun getMp3Metadata(musicSource: MusicSource.Local): Metadata?