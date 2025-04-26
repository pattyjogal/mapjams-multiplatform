package al.pattyjog.mapjams.ui

import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionResultCallback
import al.pattyjog.mapjams.geo.GeofenceManager
import al.pattyjog.mapjams.music.Metadata
import al.pattyjog.mapjams.music.MusicSource
import al.pattyjog.mapjams.ui.components.AlbumArt
import al.pattyjog.mapjams.ui.theme.AppTypography
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun Home(
    onOpenMapList: () -> Unit,
) {
    val geofenceManager: GeofenceManager = koinInject()
    val isTrackingLocation by geofenceManager.isTracking.collectAsState(false)
    var checked by remember { mutableStateOf(isTrackingLocation) }

    val locationViewModel: LocationViewModel = koinInject()
    val location by locationViewModel.locationFlow.collectAsState()
    val activeMap by locationViewModel.activeMapFlow.collectAsState()
    val activeRegion by locationViewModel.regionFlow.collectAsState()

    val koin = getKoin()
    var isFineLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isLocationPermissionGranted()
        )
    }
    var isBackgroundLocationPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
        )
    }
    val isDocumentAccessPermissionNeeded by remember {
        mutableStateOf(
            activeMap?.regions?.any { it.musicSource is MusicSource.Local } == true
        )
    }
    var isDocumentAccessPermissionGranted by remember {
        mutableStateOf(
            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
        )
    }

    fun requestPermission() {
        koin.get<PermissionBridge>()
            .requestLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    isFineLocationPermissionGranted =
                        koin.get<PermissionBridge>().isLocationPermissionGranted()
                }

                override fun onPermissionDenied(
                    isPermanentDenied: Boolean
                ) {
                    isFineLocationPermissionGranted =
                        koin.get<PermissionBridge>().isLocationPermissionGranted()
                }
            })
        koin.get<PermissionBridge>()
            .requestBackgroundLocationPermission(object : PermissionResultCallback {
                override fun onPermissionGranted() {
                    isBackgroundLocationPermissionGranted =
                        koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
                }

                override fun onPermissionDenied(
                    isPermanentDenied: Boolean
                ) {
                    Logger.w { "Background location permission denied. Permanent? $isPermanentDenied" }
                    isBackgroundLocationPermissionGranted =
                        koin.get<PermissionBridge>().isBackgroundLocationPermissionGranted()
                }
            })

        if (isDocumentAccessPermissionNeeded) {
            koin.get<PermissionBridge>()
                .requestDocumentAccessPermission(object : PermissionResultCallback {
                    override fun onPermissionGranted() {
                        isDocumentAccessPermissionGranted =
                            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
                    }

                    override fun onPermissionDenied(
                        isPermanentDenied: Boolean
                    ) {
                        isDocumentAccessPermissionGranted =
                            koin.get<PermissionBridge>().isDocumentAccessPermissionGranted()
                    }
                })
        }
    }

    val metadata = remember { mutableStateOf<Metadata?>(null) }

    LaunchedEffect(isTrackingLocation) {
        checked = isTrackingLocation
    }

    LaunchedEffect(activeRegion) {
        if (activeRegion?.musicSource != null) {
            metadata.value = activeRegion?.musicSource?.getMetadata()
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.safeDrawingPadding().navigationBarsPadding()

            )
            {
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (activeRegion == null) {
                        Text(
                            "Step into a region to hear the jams!",
                            style = AppTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        AlbumArt(
                            metadata.value,
                            size = 64.dp,
                            rounded = true,
                        )
                    }
                    FloatingActionButton(
                        onClick = {},
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (location != null) {
                PlatformMapDisplayComponent(
                    regions = activeMap?.regions ?: emptyList(),
                    currentLocation = location!! // TODO: This feels hacky
                )
            } else if (checked) {
                Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 10.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth().heightIn(min = 128.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!isFineLocationPermissionGranted || !isBackgroundLocationPermissionGranted) {
                                Text(
                                    "MapJams requires background location permissions to function. Location is only monitored when the switch is toggled.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (!isFineLocationPermissionGranted) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Warning,
                                            contentDescription = "Permission issue"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Location permissions are not granted. Please grant precise location permissions when prompted.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                if (!isBackgroundLocationPermissionGranted) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Warning,
                                            contentDescription = "Permission issue"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Background location permissions are not granted. Please select \"Allow all the time\" when prompted.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                AnimatedContent(
                                    targetState = checked,
                                    transitionSpec = {
                                        (fadeIn() + slideInHorizontally()).togetherWith(fadeOut() + slideOutHorizontally())
                                    }) { targetChecked ->
                                    if (targetChecked) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Region", style = AppTypography.labelSmall)
                                            Text(
                                                activeRegion?.name ?: "--",
                                                style = AppTypography.headlineMedium
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.height(128.dp)
                                        ) {
                                            Text(
                                                text = "Ready for an adventure?",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontStyle = FontStyle.Italic,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Switch(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onCheckedChange = {
                                checked = it
                                if (!isFineLocationPermissionGranted || !isBackgroundLocationPermissionGranted || (isDocumentAccessPermissionNeeded && !isDocumentAccessPermissionGranted)) {
                                    checked = false
                                    requestPermission()
                                    Logger.v { "Requesting permission: isFineLocationPermissionGranted: $isFineLocationPermissionGranted" }
                                    Logger.v { "Requesting permission: isBackgroundLocationPermissionGranted: $isBackgroundLocationPermissionGranted" }
                                    Logger.v { "Requesting permission: isDocumentAccessPermissionNeeded: $isDocumentAccessPermissionNeeded, isDocumentAccessPermissionGranted: $isDocumentAccessPermissionGranted" }
                                } else {
                                    if (checked && !isTrackingLocation) {
                                        Logger.v { "Requesting start monitoring" }
                                        geofenceManager.startMonitoring()
                                    } else if (!checked && isTrackingLocation) {
                                        Logger.v { "Requesting stop monitoring" }
                                        geofenceManager.stopMonitoring()
                                    }
                                }
                            },
                            checked = checked
                        )
                    }
                }
                Button(onClick = {
                    onOpenMapList()
                },
                    elevation = ButtonDefaults.elevatedButtonElevation()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeMap == null) {
                            Icon(Icons.Rounded.Map, contentDescription = "Select map")
                            Text("Select map")
                        } else {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = "Change map")
                            Text(activeMap?.name ?: "Invalid Map")
                        }
                    }
                }
            }
        }
    }
}