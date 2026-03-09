package net.kibotu.jsbridge.commands

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Handles multiple navigation patterns: back, close, and URL navigation.
 *
 * Back navigation delegates to [AppCompatActivity.onBackPressedDispatcher] so the
 * host app's back-handling callbacks (e.g., Predictive Back) are respected. Close
 * simply finishes the Activity. URL navigation is intentionally left as a hook --
 * routing logic belongs in the host app, not the bridge library.
 */
class NavigationCommand(private val contextProvider: () -> Context?) : BridgeCommand {

    override val action = "navigation"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val url = BridgeParsingUtils.parseString(content, "url")
            val external = BridgeParsingUtils.parseBoolean(content, "external")
            val goBack = BridgeParsingUtils.parseBoolean(content, "goBack")
            val close = BridgeParsingUtils.parseBoolean(content, "close")

            Timber.i("[handle] url=$url external=$external goBack=$goBack close=$close")

            when {
                goBack == true -> {
                    val activity =
                        BridgeContextProvider.findActivity(contextProvider()) as? AppCompatActivity
                    if (activity == null) {
                        return@withContext BridgeResponseUtils.createErrorResponse(
                            "NO_ACTIVITY",
                            "No active activity"
                        )
                    }
                    activity.onBackPressedDispatcher.onBackPressed()
                }

                close == true -> {
                    val activity = BridgeContextProvider.findActivity(contextProvider())
                    if (activity == null) {
                        return@withContext BridgeResponseUtils.createErrorResponse(
                            "NO_ACTIVITY",
                            "No active activity"
                        )
                    }
                    activity.finish()
                }

                url.isNotEmpty() -> {
                    // URL navigation -- implement via your app's navigation service
                }

                else -> {
                    return@withContext BridgeResponseUtils.createErrorResponse(
                        "INVALID_PARAMETER",
                        "Missing navigation parameter"
                    )
                }
            }

            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "NAVIGATION_FAILED",
                e.message ?: "Failed to navigate"
            )
        }
    }
}
