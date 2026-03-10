package net.kibotu.jsbridge.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.JavaScriptBridge
import net.kibotu.jsbridge.SecureStorage
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Persists sensitive data to Android's encrypted storage.
 *
 * **Why web needs this:**
 * Web's localStorage/sessionStorage are NOT secure:
 * - Stored as plaintext (accessible via adb, root, file explorers)
 * - Can be read by malicious JavaScript
 * - Not encrypted at rest
 * Native encrypted storage protects sensitive data using Android Keystore.
 *
 * **Why encrypted storage:**
 * Critical for storing:
 * - Authentication tokens/refresh tokens
 * - User credentials (if needed)
 * - API keys
 * - PII that should never be in plaintext
 *
 * **Why Dispatchers.IO:**
 * Storage operations are I/O bound. IO dispatcher optimized for blocking
 * operations, prevents blocking main thread during disk writes.
 *
 * **Why key-value model:**
 * Simple, familiar pattern (like localStorage API). Web developers understand
 * key-value storage immediately, reducing learning curve.
 */
class SaveSecureDataCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    private val secureStorage by lazy {
        val context = BridgeContextProvider.findActivity(bridge?.context) ?: bridge?.context
        SecureStorage(requireNotNull(context))
    }

    override val action = "saveSecureData"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.IO) {
        val key = BridgeParsingUtils.parseString(content, "key")
        val value = BridgeParsingUtils.parseString(content, "value")

        if (key.isNullOrEmpty()) {
            return@withContext BridgeResponseUtils.createErrorResponse(
                "INVALID_PARAMETER",
                "Missing 'key' parameter"
            )
        }

        try {
            secureStorage.save(key, value)
            Timber.i("[handle] saved key=$key, valueLength=${value?.length}")
            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "SAVE_FAILED",
                e.message ?: "Failed to save data"
            )
        }
    }
}

