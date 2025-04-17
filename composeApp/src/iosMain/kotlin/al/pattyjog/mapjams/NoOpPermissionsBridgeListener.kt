package al.pattyjog.mapjams

class NoOpPermissionsBridgeListener : PermissionsBridgeListener {
    override fun requestLocationPermission(callback: PermissionResultCallback) {
        callback.onPermissionGranted()
    }

    override fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        callback.onPermissionGranted()
    }

    override fun isLocationPermissionGranted(): Boolean = true

    override fun isBackgroundLocationPermissionGranted(): Boolean = true

    override fun requestDocumentAccessPermission(callback: PermissionResultCallback) {
        callback.onPermissionGranted()
    }

    override fun isDocumentAccessPermissionGranted(): Boolean = true
}