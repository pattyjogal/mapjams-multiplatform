package al.pattyjog.mapjams

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidModule
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import commonModule
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

// https://medium.com/@rishabh1112131415/how-to-handle-permissions-in-kotlin-multiplatform-without-external-libraries-05d7203237e3
class MainActivity : ComponentActivity(), PermissionsBridgeListener {
    private var fineLocationPermissionResultCallback: PermissionResultCallback? = null
    private var backgroundLocationPermissionResultCallback: PermissionResultCallback? = null

    private val requestFineLocationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fineLocationPermissionResultCallback?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                fineLocationPermissionResultCallback?.onPermissionDenied(permanentlyDenied)
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestBackgroundLocationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                backgroundLocationPermissionResultCallback?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                backgroundLocationPermissionResultCallback?.onPermissionDenied(permanentlyDenied)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileKit.init(this)

        startKoin {
            androidContext(this@MainActivity)
            modules(listOf(commonModule, androidModule))
        }

        GlobalContext.get().get<PermissionBridge>().listener = this

        setContent {
            App()
        }
    }

    override fun requestLocationPermission(callback: PermissionResultCallback) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                callback.onPermissionDenied(false)
            }

            else -> {
                fineLocationPermissionResultCallback = callback
                requestFineLocationPermissionLauncher.launch(permission)
            }
        }
    }

    override fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            callback.onPermissionGranted()
            return
        }
        val permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                callback.onPermissionDenied(false)
            }

            else -> {
                backgroundLocationPermissionResultCallback = callback
                requestBackgroundLocationPermissionLauncher.launch(permission)
            }
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isBackgroundLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}