package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.Map
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object MapList

@Serializable
data class MapDetail(val id: String)

@Serializable
data class EditRegion(val id: String)

@Composable
fun AppNavigation(maps: List<Map>) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MapList) {
        composable<MapList> {
            MapListScreen(
                onMapClick = {
                    navController.navigate(MapDetail(id = it.id))
                }
            )
        }

        composable<MapDetail> { backStackEntry ->
            val mapDetail: MapDetail = backStackEntry.toRoute()
            val selectedMap = maps.find { it.id == mapDetail.id }

            if (selectedMap != null) {
                MapDetailScreen(
                    mapId = selectedMap.id,
                    onRegionEdit = {
                        navController.navigate(EditRegion(id = it.id))
                    },
                    onRegionDelete = {},
                    onRegionCreate = {}
                )
            } else {
                Text("Map not found")
            }
        }

        composable<EditRegion> { backStackEntry ->
            val region: EditRegion = backStackEntry.toRoute()
            // TODO: Performant lookup
            val selectedRegion = maps.flatMap { it.regions }.find { it.id == region.id }

            if (selectedRegion != null) {
                RegionEditScreen(
                    initialRegion = selectedRegion,
                    onRegionSave = TODO(),
                )
            } else {
                Text("Region not found")
            }
        }
    }
}