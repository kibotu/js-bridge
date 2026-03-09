package net.kibotu.jsbridge.commands

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kibotu.jsbridge.BridgeContextProvider
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import org.json.JSONObject
import timber.log.Timber

/**
 * Triggers haptic feedback so web content can provide tactile responses to user actions.
 *
 * On API < 33, uses [android.view.View.performHapticFeedback] with
 * [HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING] to ensure feedback even when
 * the user has disabled system haptics.
 *
 * On API 33+ that flag was deprecated and silently ignored for non-privileged apps.
 * [performHapticFeedback] may return true while producing no vibration when the global
 * haptic toggle is off. Therefore, on API 33+ we use predefined [VibrationEffect]
 * constants (e.g. [VibrationEffect.EFFECT_CLICK]) which bypass the haptic toggle and
 * are device-optimised. This requires the VIBRATE permission.
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

                val context = requireNotNull(
                    BridgeContextProvider.findActivity(contextProvider()) ?: contextProvider()
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    performHapticViaVibrator(context)
                } else {
                    performHapticViaView(context)
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
     * API 33+: Uses predefined [VibrationEffect] constants which are device-optimised
     * and not gated by the global haptic feedback toggle. Falls back to
     * [android.view.View.performHapticFeedback] as a last resort.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun performHapticViaVibrator(context: Context) {
        val vibrator = getVibrator(context)

        if (vibrator != null && vibrator.hasVibrator()) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
            return
        }

        Timber.d("[performHapticViaVibrator] Vibrator unavailable, attempting view fallback")
        performHapticViaViewFallback(context)
    }

    /**
     * API < 33: Uses [android.view.View.performHapticFeedback] with deprecated flags
     * to ignore both view-level and global haptic settings. Falls back to [Vibrator]
     * if no Activity window is available.
     */
    private fun performHapticViaView(context: Context) {
        val activity = context as? Activity ?: BridgeContextProvider.findActivity(context)
        val view = activity?.window?.decorView

        if (view != null) {
            val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }

            @Suppress("DEPRECATION")
            view.performHapticFeedback(
                constant,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            return
        }

        Timber.d("[performHapticViaView] No decor view, falling back to Vibrator")
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun performHapticViaViewFallback(context: Context) {
        val activity = context as? Activity ?: BridgeContextProvider.findActivity(context) ?: return
        val view = activity.window?.decorView ?: return
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

