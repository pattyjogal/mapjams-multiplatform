package al.pattyjog.mapjams

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import cocoapods.FirebaseCrashlytics.FIRCrashlytics // Adjust import if needed
import kotlinx.cinterop.ExperimentalForeignApi

class FirebaseKermitWriter(
    private val minSeverity: Severity = Severity.Info, // Log Info and above
    private val minCrashSeverity: Severity = Severity.Warn // Send Warn and above as non-fatal
) : LogWriter() {

    @OptIn(ExperimentalForeignApi::class)
    private val crashlytics = FIRCrashlytics.crashlytics()

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity

    @OptIn(ExperimentalForeignApi::class)
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // Log messages to Crashlytics logs
        crashlytics.log("$severity: [$tag] $message")

        if (throwable != null && severity >= minCrashSeverity) {
            // Log exceptions/errors to Crashlytics as non-fatal errors
            val userInfo = mutableMapOf<Any?, Any>(
                "KermitTag" to tag,
                "KermitSeverity" to severity.name
            )
            // Basic Throwable to NSError conversion (can be enhanced)
            val nsError = platform.Foundation.NSError.errorWithDomain(
                "KotlinException",
                code = throwable.hashCode().toLong(), // Basic code
                userInfo = userInfo + mapOf(platform.Foundation.NSLocalizedDescriptionKey to (throwable.message ?: "No message"))
            )
            crashlytics.recordError(nsError)
        }
    }
}