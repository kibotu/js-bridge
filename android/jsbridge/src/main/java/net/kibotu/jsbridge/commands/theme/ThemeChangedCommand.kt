package net.kibotu.jsbridge.commands.theme

import net.kibotu.jsbridge.commands.BridgeCommand
import net.kibotu.jsbridge.commands.utils.BridgeParsingUtils
import net.kibotu.jsbridge.commands.utils.BridgeResponseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * Forwards theme change requests from web content to [ThemeService].
 *
 * The bridge intentionally does not apply the theme itself -- it just relays the
 * preference. This keeps the library decoupled from any app-level ThemeManager and
 * lets the host app decide how (and whether) to honor the request.
 */
class ThemeChangedCommand : BridgeCommand {

    override val action = "themeChanged"

    override suspend fun handle(content: Any?): JSONObject = withContext(Dispatchers.Main) {
        try {
            val theme = BridgeParsingUtils.parseString(content, "theme").ifEmpty { "dark" }

            Timber.i("[ThemeChangedCommand] theme=$theme")

            ThemeService.setTheme(theme)

            BridgeResponseUtils.createSuccessResponse()
        } catch (e: Exception) {
            Timber.e(e)
            BridgeResponseUtils.createErrorResponse(
                "THEME_CHANGE_FAILED",
                e.message ?: "Failed to change theme"
            )
        }
    }
}
