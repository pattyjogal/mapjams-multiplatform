package al.pattyjog.mapjams.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import org.koin.core.context.GlobalContext

actual suspend fun getMp3Metadata(musicSource: MusicSource.Local): Metadata? {
    val context: Context = GlobalContext.get().get()
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, musicSource.file.toUri())
        return Metadata().run {
            copy(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: title,
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: artist,
                artwork = retriever.embeddedPicture ?: artwork
            )
        }
    } catch (e: Exception) {
        Log.e("getMp3Metadata", "Encountered error while extracting metadata", e)
        return null
    } finally {
        retriever.release()
    }
}