package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

const val MAPS_GRAPH_ROUTE = "maps_root"

@Serializable
data object Home

@Serializable
data object MapList

@Serializable
data class MapDetail(val id: String)

@Serializable
data class EditRegion(val id: String)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedVm: MapViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Home,
    ) {
        composable<Home> {
            Home(
                onOpenMapList = {
                    navController.navigate(MapList)
                },
            )
        }

        composable<MapList> {
            MapListScreen(
                onMapClick = {
                    navController.navigate(MapDetail(id = it.id))
                },
                vm = sharedVm
            )
        }

        composable<MapDetail> { backStackEntry ->
            val mapDetail: MapDetail = backStackEntry.toRoute()

            MapDetailScreen(
                mapId = mapDetail.id,
                onRegionEdit = {
                    navController.navigate(EditRegion(id = it.id))
                },
                vm = sharedVm
            )
        }

        composable<EditRegion> { backStackEntry ->
            val region: EditRegion = backStackEntry.toRoute()

            RegionEditScreen(
                initialRegionId = region.id,
                onRegionSave = {
                    navController.popBackStack()
                },
                vm = sharedVm
            )
        }
    }
}
