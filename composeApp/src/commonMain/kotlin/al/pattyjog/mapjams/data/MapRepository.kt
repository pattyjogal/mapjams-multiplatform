package al.pattyjog.mapjams.data

import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.data.Map as MapEntity

interface MapRepository {
    suspend fun findMap(id: String): Map?
    suspend fun getMaps(): List<Map>
    suspend fun addMaps(maps: List<Map>)
    suspend fun deleteMap(id: String)
}

class MapRepositoryImpl(val db: MapJamsDatabase) : MapRepository {
    override suspend fun findMap(id: String): Map? {
        val mapRow = db.geoQueries.selectMapById(id).executeAsOneOrNull()
        return mapRow?.let { rowToMap(it) }
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
                    musicSource = MusicSource.Local(regionRow.musicSource)
                )
            }
        )
    }

    override suspend fun addMaps(maps: List<Map>) {
        maps.forEach { map ->
            db.geoQueries.insertMap(map.id, map.name)

            // TODO: Insert regions
        }
    }

    override suspend fun deleteMap(id: String) {
        TODO("Not yet implemented")
    }

    private fun deserializePolygon(json: String): List<LatLng> {
        // TODO: Implement your JSON deserialization logic here.
        return emptyList()
    }
}