import al.pattyjog.mapjams.MapJamsDatabase
import al.pattyjog.mapjams.PermissionBridge
import al.pattyjog.mapjams.PermissionsViewModel
import al.pattyjog.mapjams.PlatformHaptic
import al.pattyjog.mapjams.geo.AndroidGeofenceManger
import al.pattyjog.mapjams.geo.GeofenceManager
import al.pattyjog.mapjams.music.AndroidMusicController
import al.pattyjog.mapjams.music.MusicController
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.lexilabs.basic.haptic.Haptic
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

    viewModel {
        PermissionsViewModel(
            permissionsController = PermissionsController(applicationContext = androidContext())
        )
    }

    single<GeofenceManager> { AndroidGeofenceManger(androidContext()) }

    single<MusicController> { AndroidMusicController(androidContext()) }

    single<PlatformHaptic> {
        object : PlatformHaptic {
            val vibrator: Vibrator? = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // Android 12+ has a VibratorManager
                    val vm = androidContext().getSystemService(VibratorManager::class.java)
                    vm?.defaultVibrator
                }

                else -> {
                    // Pre‑Android‑12 just use the class‑based getSystemService
                    androidContext().getSystemService(Vibrator::class.java)
                }
            }

            override fun shortBuzz() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // One‑shot vibration for 50 ms at default amplitude
                    val effect =
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator?.vibrate(effect)
                } else {
                    // Fallback on older devices
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        }
    }
}