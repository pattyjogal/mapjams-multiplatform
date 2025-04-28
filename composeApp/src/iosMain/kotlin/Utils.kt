import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLBookmarkResolutionWithSecurityScope
import platform.Foundation.create

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
fun bookmarkToUrl(base64: String): NSURL {
    val data  = NSData.create(base64Encoding = base64)!!
    memScoped {
        val stale = alloc<BooleanVar>()

        val url = NSURL.URLByResolvingBookmarkData(
            data,
            options = NSURLBookmarkResolutionWithSecurityScope, // must match
            relativeToURL = null,
            bookmarkDataIsStale = stale.ptr,
            error = null
        ) ?: error("Could not resolve bookmark")

        if (stale.value) {
            // Optionally create a fresh bookmark and overwrite the DB
        }
        return url
    }
}