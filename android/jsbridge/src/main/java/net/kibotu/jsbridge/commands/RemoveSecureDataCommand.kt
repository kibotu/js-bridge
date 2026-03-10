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
 * Deletes sensitive data from Android's encrypted storage.
 *
 * **Why web needs this:**
 * Critical for:
 * - User logout (clear auth tokens, session data)
 * - Data privacy (remove cached sensitive info)
 * - Storage management (clean up obsolete data)
 * - Security best practice (don't keep data longer than needed)
 *
 * **Why separate from save:**
 * Explicit deletion operation makes security-critical actions visible in code.
 * Can't accidentally delete by saving null/empty (explicit is better than implicit).
 *
 * **Why Dispatchers.IO:**
 * Storage deletion is disk I/O operation, should not block main thread.
 *
 * **Common use cases:**
 * - Logout flow: remove all auth tokens
 * - Privacy compliance: user requests data deletion
 * - Session management: clear expired/invalid tokens
 */
class RemoveSecureDataCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    private val secureStorage by lazy {
        val context = BridgeContextProvider.findActivity(bridge?.context) ?: bridge?.context
        SecureStorage(requireNotNull(context))
    }

    override val action = "removeSecureData"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.IO) {
        val key = BridgeParsingUtils.parseString(content, "key")

        if (key.isNullOrEmpty()) {
            return@withContext BridgeResponseUtils.createErrorResponse(
                "INVALID_PARAMETER",
                "Missing 'key' parameter"
            )
        }

        try {
            val keyExisted = secureStorage.contains(key)
            secureStorage.remove(key)
            Timber.i("[handle] removed key=$key, existed=$keyExisted")
            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "REMOVE_FAILED",
                e.message ?: "Failed to remove data"
            )
        }
    }
}