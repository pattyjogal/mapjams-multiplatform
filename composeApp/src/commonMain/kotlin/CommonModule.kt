import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.data.MapRepository
import al.pattyjog.mapjams.data.MapRepositoryImpl
import al.pattyjog.mapjams.data.MapViewModel
import al.pattyjog.mapjams.geo.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule: Module = module {
    single { MapJamsDatabase(get()) }

    single<MapRepository> { MapRepositoryImpl(db = get()) }

    single<PermissionBridge> { PermissionBridge() }

    viewModel { MapViewModel(repository = get()) }

    single { MutableStateFlow<LatLng?>(null) }
}