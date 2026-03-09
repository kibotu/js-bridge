package net.kibotu.jsbridge.commands

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * Triggers haptic feedback so web content can provide tactile responses to user actions.
 *
 * Prefers [android.view.View.performHapticFeedback] because it respects system haptic
 * settings and requires no VIBRATE permission. Falls back to [Vibrator] only when the
 * view-level API is unavailable (e.g., no Activity window).
 */
class HapticCommand(private val contextProvider: () -> Context?) : BridgeCommand {

    override val action = "haptic"

    override suspend fun handle(content: Any?): JSONObject =
        withContext(Dispatchers.Main) {
            try {
                val vibrate = BridgeParsingUtils.parseBoolean(content, "vibrate")

                Timber.i("[handle] vibrate=$vibrate")

                if (vibrate == false) {
                    return@withContext BridgeResponseUtils.createSuccessResponse()
                }

                val context = requireNotNull(BridgeContextProvider.findActivity(contextProvider()) ?: contextProvider())

                if (tryPerformHapticFeedback(context)) {
                    return@withContext BridgeResponseUtils.createSuccessResponse()
                }

                Timber.d("[handle] performHapticFeedback unavailable, falling back to Vibrator")

                val vibrator = getVibrator(context)
                    ?: return@withContext BridgeResponseUtils.createErrorResponse(
                        "VIBRATOR_UNAVAILABLE",
                        "Vibrator service not available"
                    )

                if (!vibrator.hasVibrator()) {
                    return@withContext BridgeResponseUtils.createErrorResponse(
                        "VIBRATOR_UNAVAILABLE",
                        "Device does not have a vibrator"
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }

                BridgeResponseUtils.createSuccessResponse()
            } catch (e: Exception) {
                Timber.e(e)
                BridgeResponseUtils.createErrorResponse(
                    "HAPTIC_FAILED",
                    e.message ?: "Failed to trigger haptic feedback"
                )
            }
        }

    /**
     * Uses [android.view.View.performHapticFeedback] on the Activity's decor view.
     * This requires no VIBRATE permission and works reliably across OEMs on API 36+.
     */
    private fun tryPerformHapticFeedback(context: Context): Boolean {
        val activity = context as? Activity ?: BridgeContextProvider.findActivity(context) ?: return false
        val view = activity.window?.decorView ?: return false

        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            view.performHapticFeedback(constant)
        } else {
            @Suppress("DEPRECATION")
            view.performHapticFeedback(
                constant,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

