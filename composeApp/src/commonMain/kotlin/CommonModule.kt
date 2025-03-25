import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.data.MapRepository
import al.pattyjog.mapjams.data.MapRepositoryImpl
import al.pattyjog.mapjams.data.MapViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule: Module = module {
    single { MapJamsDatabase(get()) }

    single<MapRepository> { MapRepositoryImpl(db = get()) }

    viewModel { MapViewModel(repository = get()) }
}