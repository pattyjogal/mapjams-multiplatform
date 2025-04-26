import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.NoOpPermissionsBridgeListener
import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.geo.GeofenceManager
import al.pattyjog.mapjams.geo.IosGeofenceManager
import al.pattyjog.mapjams.music.IosMusicController
import al.pattyjog.mapjams.music.MusicController
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.context.startKoin
import org.koin.dsl.module

val iosModule = module {
    single<GeofenceManager> { IosGeofenceManager() }

    single<MusicController> { IosMusicController() }

    single<SqlDriver> {
        NativeSqliteDriver(
            schema = MapJamsDatabase.Schema,
            name = "mapjams.db"
        )
    }

    single<PermissionBridge> { PermissionBridge(NoOpPermissionsBridgeListener()) }
}

fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}
