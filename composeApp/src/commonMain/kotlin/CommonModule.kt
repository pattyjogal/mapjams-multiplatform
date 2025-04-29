import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.data.MapRepository
import al.pattyjog.mapjams.data.MapRepositoryImpl
import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.LatLng
import al.pattyjog.mapjams.geo.LocationUpdate
import al.pattyjog.mapjams.geo.Map
import al.pattyjog.mapjams.geo.Region
import al.pattyjog.mapjams.ui.LocationViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonModule: Module = module {
    single { MapJamsDatabase(get()) }

    single<MapRepository> { MapRepositoryImpl(db = get()) }

    single<PermissionBridge> { PermissionBridge() }

    single(named("locationFlow")) { MutableStateFlow<LatLng?>(null) }
    single(named("rawLocationFlow")) { MutableSharedFlow<LocationUpdate?>(extraBufferCapacity = 64) }
    single(named("regionFlow")) { MutableStateFlow<Region?>(null) }
    single(named("activeMapFlow")) { MutableStateFlow<Map?>(null) }
    single(named("isPlayingFlow")) { MutableStateFlow(false) }

    viewModel { MapViewModel(
        activeMapFlow = get(named("activeMapFlow")),
        repository = get()
    ) }
    viewModel {
        LocationViewModel(
            _rawLocationFlow = get(named("rawLocationFlow")),
            _locationFlow = get(named("locationFlow")),
            _regionFlow = get(named("regionFlow")),
            _activeMapFlow = get(named("activeMapFlow")),
            musicController = get()
        )
    }
}