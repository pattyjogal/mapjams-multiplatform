package al.pattyjog.mapjams

import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLBookmarkResolutionWithSecurityScope
import platform.Foundation.create

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
fun bookmarkToUrl(base64: String): NSURL? = memScoped {
    val data = NSData.create(base64Encoding = base64)
    if (data == null) {
        Logger.e { "bookmarkToUrl: invalid Base-64 string" }
        return null
    }

    val errPtr = alloc<ObjCObjectVar<NSError?>>()
    val stale = alloc<BooleanVar>()

    try {
        val url = NSURL.URLByResolvingBookmarkData(
            data,
            options = NSURLBookmarkResolutionWithSecurityScope,
            relativeToURL = null,
            bookmarkDataIsStale = stale.ptr,
            error = errPtr.ptr
        )
        if (url == null) {
            val reason = errPtr.value?.localizedDescription ?: "unknown error"
            Logger.e { "bookmarkToUrl: could not resolve bookmark – $reason" }
            return null
        }
        if (stale.value) {
            Logger.w { "bookmarkToUrl: bookmark is stale – consider re-creating it" }
        }
        return url
    } catch (err: Throwable) {
        val reason = errPtr.value?.localizedDescription ?: "unknown error"
        Logger.e(err) { "bookmarkToUrl: could not resolve bookmark – $reason" }
    }

    return null
}
