package al.pattyjog.mapjams.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DefaultAlbumArt(
    size: Dp = 96.dp
) = Box(
    modifier = Modifier
        .background(color = MaterialTheme.colorScheme.secondaryContainer)
        .size(size),
    contentAlignment = Alignment.Center
) {
    Icon(
        Icons.Rounded.MusicNote,
        contentDescription = "Unknown album art",
        modifier = Modifier.size(size / 2)
    )
}