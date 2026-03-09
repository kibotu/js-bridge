package net.kibotu.jsbridge.commands.bottomnavigation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.JavaScriptBridge
import net.kibotu.jsbridge.SafeAreaService
import net.kibotu.jsbridge.commands.BridgeAware
import net.kibotu.jsbridge.commands.BridgeCommand
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Toggles the native bottom navigation bar visibility from web content.
 *
 * Pushes updated safe area insets after the change so web content can
 * reclaim (or yield) the space occupied by the bar without a round-trip.
 */
class BottomNavigationCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    override val action = "bottomNavigation"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val isVisible = BridgeParsingUtils.parseBoolean(content, "isVisible")

            Timber.i("[handle] isVisible=$isVisible")

            BottomNavigationService.setVisible(isVisible == true)

            SafeAreaService.pushTobridge(bridge)

            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "BOTTOM_NAVIGATION_FAILED",
                e.message ?: "Failed to configure bottom navigation"
            )
        }
    }
}