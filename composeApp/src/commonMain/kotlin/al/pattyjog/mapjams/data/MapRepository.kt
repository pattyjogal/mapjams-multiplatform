package al.pattyjog.mapjams.data

import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.data.Map as MapEntity

// BUG: Updating a region doesn't refresh viewmodel?
interface MapRepository {
    suspend fun findMap(id: String): Map?
    suspend fun findRegion(id: String): Region?
    suspend fun getMaps(): List<Map>
    suspend fun addMaps(maps: List<Map>)
    suspend fun deleteMap(id: String)
    suspend fun addRegionToMap(map: Map, region: Region)
    suspend fun deleteRegion(id: String)
    suspend fun updateRegion(region: Region)
}

class MapRepositoryImpl(val db: MapJamsDatabase) : MapRepository {
    override suspend fun findMap(id: String): Map? {
        val mapRow = db.geoQueries.selectMapById(id).executeAsOneOrNull()
        return mapRow?.let { rowToMap(it) }
    }

    override suspend fun findRegion(id: String): Region? {
        val regionRow = db.geoQueries.selectRegionById(id).executeAsOneOrNull()
        return regionRow?.let { region ->
            Region(
                id = region.id,
                name = region.name,
                polygon = deserializePolygon(region.polygon),
                musicSource = region.musicSource?.let(MusicSource::Local)
            )
        }
    }

    override suspend fun getMaps(): List<Map> {
        val mapRows = db.geoQueries.selectAllMaps().executeAsList()
        return mapRows.map(::rowToMap)
    }

    private fun rowToMap(mapRow: MapEntity): Map {
        val regionRows = db.geoQueries.selectRegionsByMapId(mapRow.id).executeAsList()
        return Map(
            id = mapRow.id,
            name = mapRow.name,
            regions = regionRows.map { regionRow ->
                Region(
                    id = regionRow.id,
                    name = regionRow.name,
                    polygon = deserializePolygon(regionRow.polygon),
                    // TODO: Fix when loading music sources for real
                    musicSource = regionRow.musicSource?.let(MusicSource::Local)
                )
            }
        )
    }

    override suspend fun addMaps(maps: List<Map>) {
        maps.forEach { map ->
            db.geoQueries.insertMap(map.id, map.name)
            map.regions.forEach { region ->
                addRegionToMap(map, region)
            }
        }
    }

    override suspend fun deleteMap(id: String) {
        db.geoQueries.deleteMap(id)
    }

    override suspend fun addRegionToMap(map: Map, region: Region) {
        db.geoQueries.insertRegion(
            id = region.id,
            mapId = map.id,
            name = region.name,
            polygon = serializePolygon(region.polygon),
            musicSource = region.musicSource.toString()
        )
    }

    override suspend fun updateRegion(region: Region) {
        db.geoQueries.updateRegion(
            name = region.name,
            polygon = serializePolygon(region.polygon),
            musicSource = when (region.musicSource) {
                is MusicSource.AppleMusic -> TODO()
                is MusicSource.Local -> region.musicSource.file
                is MusicSource.Spotify -> TODO()
                null -> null
            },
            id = region.id
        )
    }

    override suspend fun deleteRegion(id: String) {
        db.geoQueries.deleteRegion(id)
    }

    private fun serializePolygon(polygon: List<LatLng>) = polygon.joinToString(";") { "${it.latitude}:${it.longitude}" }

    private fun deserializePolygon(json: String) = json.split(";")
        .takeIf { it.size > 1 || it[0].isNotEmpty() }
        ?.map {
            val (latitude, longitude) = it.split(":")
            LatLng(latitude.toDouble(), longitude.toDouble())
        } ?: emptyList()
}