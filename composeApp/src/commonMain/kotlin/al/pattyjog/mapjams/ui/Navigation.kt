package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.geo.Map
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


@Serializable
data object Home

@Serializable
data object MapList

@Serializable
data class MapDetail(val id: String)

@Serializable
data class EditRegion(val id: String)

val bottomBarScreens = listOf(Home, MapList)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (bottomBarScreens.any { currentRoute?.hasRoute(it::class) == true }) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute!!.hasRoute<Home>(),
                        onClick = { navController.navigate(Home) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "The home map view") },
                        label = { Text("Go") }
                    )
                    NavigationBarItem(
                        selected = currentRoute.hasRoute<MapList>(),
                        onClick = { navController.navigate(MapList) },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List of all maps") },
                        label = { Text("Maps") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Home, modifier = Modifier.padding(padding)) {
            composable<Home> {
                Home()
            }

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
}