package al.pattyjog.mapjams

actual interface PermissionsBridgeListener {
    actual fun requestLocationPermission(callback: PermissionResultCallback)
    actual fun requestBackgroundLocationPermission(callback: PermissionResultCallback)
    actual fun isLocationPermissionGranted(): Boolean
    actual fun isBackgroundLocationPermissionGranted(): Boolean
}