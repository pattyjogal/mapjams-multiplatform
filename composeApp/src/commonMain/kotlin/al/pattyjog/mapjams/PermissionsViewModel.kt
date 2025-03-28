package al.pattyjog.mapjams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.BACKGROUND_LOCATION
import kotlinx.coroutines.launch

class PermissionsViewModel(val permissionsController: PermissionsController): ViewModel() {
    fun requestBackgroundLocation() {
        viewModelScope.launch {
            try {
                permissionsController.providePermission(Permission.BACKGROUND_LOCATION)
                // Permission has been granted successfully.
            } catch(deniedAlways: DeniedAlwaysException) {
                // Permission is always denied.
            } catch(denied: DeniedException) {
                // Permission was denied.
            }
        }
    }
}