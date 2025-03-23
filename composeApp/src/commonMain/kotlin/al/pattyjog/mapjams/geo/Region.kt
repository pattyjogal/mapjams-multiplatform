package al.pattyjog.mapjams.geo

import al.pattyjog.mapjams.music.MusicSource

data class Region(
    val id: String,
    val name: String,
    val polygon: List<LatLng>,
    val musicSource: MusicSource
)
