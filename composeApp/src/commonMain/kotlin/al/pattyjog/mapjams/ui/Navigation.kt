package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.Map
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
fun AppNavigation() {
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

            MapDetailScreen(
                mapId = mapDetail.id,
                onRegionEdit = {
                    navController.navigate(EditRegion(id = it.id))
                },
            )
        }

        composable<EditRegion> { backStackEntry ->
            val region: EditRegion = backStackEntry.toRoute()

                RegionEditScreen(
                    initialRegionId = region.id,
                    onRegionSave = {
                        navController.popBackStack()
                    },
                )

        }
    }
}