import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionsViewModel
import al.pattyjog.mapjams.geo.AndroidGeofenceManger
import al.pattyjog.mapjams.geo.GeofenceManager
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.icerock.moko.permissions.PermissionsController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule: Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = MapJamsDatabase.Schema,
            context = get(),
            name = "mapjams.db"
        )
    }

    viewModel { PermissionsViewModel(
        permissionsController = PermissionsController(applicationContext = androidContext())
    ) }

    single<GeofenceManager> { AndroidGeofenceManger(androidContext()) }
}