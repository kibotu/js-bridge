package net.kibotu.jsbridge.commands.topnavigation

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
 * Configures the native top navigation bar from web content.
 *
 * After applying the config, pushes updated safe area insets to the WebView so
 * web content can adjust its layout immediately (e.g., when the bar is hidden,
 * content should extend behind the status bar).
 */
class TopNavigationCommand : BridgeCommand, BridgeAware {

    override var bridge: JavaScriptBridge? = null

    override val action = "topNavigation"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val isVisible = BridgeParsingUtils.parseBoolean(content, "isVisible")
            val title = BridgeParsingUtils.parseString(content, "title")
            val showUpArrow = BridgeParsingUtils.parseBoolean(content, "showUpArrow")
            val showDivider = BridgeParsingUtils.parseBoolean(content, "showDivider")
            val showLogo = BridgeParsingUtils.parseBoolean(content, "showLogo")
            val showProfileIconWidget =
                BridgeParsingUtils.parseBoolean(content, "showProfileIconWidget")

            Timber.i("[handle] isVisible=$isVisible title=$title showUpArrow=$showUpArrow")

            TopNavigationService.applyConfig(
                TopNavigationConfig(
                    isVisible = isVisible == true,
                    title = title.takeIf { it.isNotEmpty() },
                    showUpArrow = showUpArrow == true,
                    showDivider = showDivider == true,
                    showLogo = showLogo == true,
                    showProfileIconWidget = showProfileIconWidget == true,
                    drawBehindStatusBar = isVisible == false
                )
            )

            SafeAreaService.pushTobridge(bridge)

            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "TOP_NAVIGATION_FAILED",
                e.message ?: "Failed to configure top navigation"
            )
        }
    }
}

/** Snapshot of the top navigation bar appearance, observed by the host Activity. */
data class TopNavigationConfig(
    val isVisible: Boolean = true,
    val showUpArrow: Boolean = false,
    val showDebugIcon: Boolean = true,
    val showDivider: Boolean = true,
    val showLogo: Boolean = false,
    val showProfileIconWidget: Boolean = false,
    val title: String? = null,
    val drawBehindStatusBar: Boolean = false
)