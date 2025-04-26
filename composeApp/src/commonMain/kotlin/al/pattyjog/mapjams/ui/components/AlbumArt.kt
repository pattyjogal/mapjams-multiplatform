package al.pattyjog.mapjams.ui.components

import al.pattyjog.mapjams.music.Metadata
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun AlbumArt(metadata: Metadata?, modifier: Modifier = Modifier, size: Dp = 96.dp, rounded: Boolean = false)
{
    val imageModifier = modifier
        .requiredSize(size)
        .then(if (rounded) Modifier.clip(RoundedCornerShape(16.dp)) else Modifier)

    metadata?.artwork?.let { artworkData ->
        Image(
            bitmap = artworkData.decodeToImageBitmap(),
            contentDescription = "Album art",
            modifier = imageModifier,
        )
    } ?: DefaultAlbumArt(size, modifier = modifier)
    Spacer(Modifier.width(8.dp))
}
