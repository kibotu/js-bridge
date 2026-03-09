package net.kibotu.jsbridge.commands.systembars

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.JavaScriptBridge
import net.kibotu.jsbridge.commands.BridgeAware
import net.kibotu.jsbridge.commands.BridgeCommand
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Controls Android system bars (status bar and navigation bar) for immersive experiences.
 *
 * Status bar and navigation bar are toggled independently -- some flows need
 * full-screen content but keep the navigation bar for usability.
 *
 * Delegates to the [Window] extensions in this package, which use
 * [WindowInsetsControllerCompat] under the hood.
 */
class SystemBarsCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    override val action = "systemBars"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val showStatusBar = BridgeParsingUtils.parseBoolean(content, "showStatusBar")
            val showSystemNavigation =
                BridgeParsingUtils.parseBoolean(content, "showSystemNavigation")

            Timber.i("[handle] showStatusBar=$showStatusBar showSystemNavigation=$showSystemNavigation")

            val activity = BridgeContextProvider.findActivity(bridge?.context)
                ?: return@withContext BridgeResponseUtils.createErrorResponse(
                    "NO_ACTIVITY",
                    "No active activity"
                )

            activity.window?.showSystemStatusBar = showStatusBar == true
            activity.window?.showSystemNavigationBar = showSystemNavigation == true

            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            BridgeResponseUtils.createErrorResponse(
                "SYSTEM_BARS_FAILED",
                e.message ?: "Failed to configure system bars"
            )
        }
    }
}

