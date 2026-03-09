package net.kibotu.jsbridge.commands

import android.content.Context
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Returns current system inset values so web content can adapt its layout.
 *
 * Web usage:
 * ```javascript
 * const insets = await jsbridge.call({ data: { action: 'insets' } });
 * ```
 */
class GetInsetsCommand(private val contextProvider: () -> Context?) : BridgeCommand {

    override val action = "insets"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val activity = BridgeContextProvider.findActivity(contextProvider())
                ?: return@withContext BridgeResponseUtils.createErrorResponse(
                    "NO_ACTIVITY",
                    "No active activity"
                )

            val decorView = activity.window.decorView
            val rootInsets = decorView.rootWindowInsets

            val result = JSONObject()

            if (rootInsets != null) {
                val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(rootInsets, decorView)
                val statusBars = insetsCompat.getInsets(WindowInsetsCompat.Type.statusBars())
                val navBars = insetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars())
                val ime = insetsCompat.getInsets(WindowInsetsCompat.Type.ime())

                result.put("statusBar", JSONObject().apply {
                    put("height", statusBars.top)
                    put("visible", insetsCompat.isVisible(WindowInsetsCompat.Type.statusBars()))
                })
                result.put("systemNavigation", JSONObject().apply {
                    put("height", navBars.bottom)
                    put("visible", insetsCompat.isVisible(WindowInsetsCompat.Type.navigationBars()))
                })
                result.put("keyboard", JSONObject().apply {
                    put("height", ime.bottom)
                    put("visible", insetsCompat.isVisible(WindowInsetsCompat.Type.ime()))
                })
                result.put("safeArea", JSONObject().apply {
                    put("top", statusBars.top)
                    put("right", navBars.right)
                    put("bottom", navBars.bottom)
                    put("left", navBars.left)
                })
            } else {
                result.put("statusBar", JSONObject().apply {
                    put("height", 0)
                    put("visible", true)
                })
                result.put("systemNavigation", JSONObject().apply {
                    put("height", 0)
                    put("visible", true)
                })
                result.put("keyboard", JSONObject().apply {
                    put("height", 0)
                    put("visible", false)
                })
                result.put("safeArea", JSONObject().apply {
                    put("top", 0)
                    put("right", 0)
                    put("bottom", 0)
                    put("left", 0)
                })
            }

            Timber.i("[handle] insets=$result")
            result
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "INSETS_FAILED",
                e.message ?: "Failed to get insets"
            )
        }
    }
}
