package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.Map
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
