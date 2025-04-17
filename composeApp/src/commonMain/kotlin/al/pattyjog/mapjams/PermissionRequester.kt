package al.pattyjog.mapjams

expect interface PermissionsBridgeListener {
    fun requestLocationPermission(callback: PermissionResultCallback)
    fun requestBackgroundLocationPermission(callback: PermissionResultCallback)
    fun requestDocumentAccessPermission(callback: PermissionResultCallback)
    fun isLocationPermissionGranted(): Boolean
    fun isBackgroundLocationPermissionGranted(): Boolean
    fun isDocumentAccessPermissionGranted(): Boolean
}

class PermissionBridge(
    var listener: PermissionsBridgeListener? = null
) {
    fun requestLocationPermission(callback: PermissionResultCallback) {
        listener?.requestLocationPermission(callback) ?: error("Callback handler not set")
    }

    fun isLocationPermissionGranted(): Boolean {
        return listener?.isLocationPermissionGranted() ?: false
    }

    fun requestBackgroundLocationPermission(callback: PermissionResultCallback) {
        listener?.requestBackgroundLocationPermission(callback) ?: error("Callback handler not set")
    }

    fun isBackgroundLocationPermissionGranted(): Boolean {
        return listener?.isBackgroundLocationPermissionGranted() ?: false
    }

    fun requestDocumentAccessPermission(callback: PermissionResultCallback) {
        listener?.requestDocumentAccessPermission(callback) ?: error("Callback handler not set")
    }

    fun isDocumentAccessPermissionGranted(): Boolean {
        return listener?.isDocumentAccessPermissionGranted() ?: false
    }
}

interface PermissionResultCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(isPermanentDenied: Boolean)
}