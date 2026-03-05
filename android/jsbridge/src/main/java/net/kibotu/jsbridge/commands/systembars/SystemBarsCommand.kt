package net.kibotu.jsbridge.commands.systembars

import android.content.Context
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import net.kibotu.jsbridge.commands.BridgeCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * Controls Android system UI (status/navigation bars) for immersive experiences.
 *
 * **Why web needs this:**
 * Web cannot control native system UI bars. Needed for:
 * - Full-screen media viewing (hide bars for immersive video/images)
 * - Games or interactive content requiring maximum screen space
 * - Specific UX flows where native chrome should disappear
 *
 * **Why separate controls:**
 * Status bar (top) and navigation bar (bottom) serve different purposes.
 * Some flows want full-screen but keep navigation for usability.
 *
 * **Why Dispatchers.Main:**
 * Window property modifications must happen on UI thread. Android will crash otherwise.
 */
class SystemBarsCommand(private val contextProvider: () -> Context?) : BridgeCommand {

    override val action = "systemBars"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val showStatusBar = BridgeParsingUtils.parseBoolean(content, "showStatusBar")
            val showSystemNavigation =
                BridgeParsingUtils.parseBoolean(content, "showSystemNavigation")

            Timber.i("[handle] showStatusBar=$showStatusBar showSystemNavigation=$showSystemNavigation")

            val activity = BridgeContextProvider.findActivity(contextProvider())
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

