package al.pattyjog.mapjams

import al.pattyjog.mapjams.geo.GeofenceManager
import al.pattyjog.mapjams.geo.IosGeofenceManager
import al.pattyjog.mapjams.music.IosMusicController
import al.pattyjog.mapjams.music.MusicController
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import commonModule
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosModule = module {
    single<GeofenceManager> { IosGeofenceManager() }

    single<MusicController> { IosMusicController(get(named("isPlayingFlow"))) }

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

    val firebaseWriter = FirebaseKermitWriter(minCrashSeverity = Severity.Warn)
    val config = StaticConfig(
        logWriterList = listOf(
            platformLogWriter(),
            firebaseWriter
        ),
    )
    Logger.setLogWriters(config.logWriterList)
    Logger.setMinSeverity(config.minSeverity)

    Logger.i { "User logged in" }
    Logger.w(throwable = Exception("Testing Crash!")) { "Something concerning happened" } // Will be sent to Crashlytics

}
