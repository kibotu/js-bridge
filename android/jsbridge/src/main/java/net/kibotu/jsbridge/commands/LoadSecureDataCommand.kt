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
 * Retrieves sensitive data from Android's encrypted storage.
 *
 * **Why web needs this:**
 * Counterpart to SaveSecureDataCommand. Web needs to retrieve previously stored
 * encrypted data (auth tokens, user settings, etc.) for:
 * - Session restoration across app restarts
 * - Authenticated API calls
 * - Persisting user preferences securely
 *
 * **Why nullable value:**
 * Returns null if key doesn't exist, allowing web to detect first-time use
 * vs. stored data. Follows familiar pattern of localStorage.getItem().
 *
 * **Why Dispatchers.IO:**
 * Reading encrypted data involves disk I/O and potentially decryption.
 * IO dispatcher prevents blocking UI during these operations.
 *
 * **Security consideration:**
 * Only web code from your domain should access this bridge. WebView's
 * origin-based security model prevents unauthorized access.
 */
class LoadSecureDataCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    private val secureStorage by lazy {
        val context = BridgeContextProvider.findActivity(bridge?.context) ?: bridge?.context
        SecureStorage(requireNotNull(context))
    }

    override val action = "loadSecureData"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.IO) {
        val key = BridgeParsingUtils.parseString(content, "key")

        if (key.isEmpty()) {
            return@withContext BridgeResponseUtils.createErrorResponse(
                "INVALID_PARAMETER",
                "Missing 'key' parameter"
            )
        }

        try {
            val value = secureStorage.load(key)
            Timber.i("[handle] loaded key=$key, valueLength=${value?.length ?: 0}")
            JSONObject().apply {
                put("key", key)
                put("value", value)
            }
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "LOAD_FAILED",
                e.message ?: "Failed to load data"
            )
        }
    }
}