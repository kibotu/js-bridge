package net.kibotu.jsbridge.commands.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Emits theme change events requested by web content.
 *
 * Uses [SharedFlow] (not [StateFlow]) because theme changes are events, not state --
 * a collector that joins late should not automatically receive the last theme.
 * The host app observes [onThemeChanged] and applies its own theming logic.
 *
 * Mirrors `ThemeService` on iOS for cross-platform parity.
 */
object ThemeService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val onThemeChanged: SharedFlow<String>
        field = MutableSharedFlow<String>()

    fun setTheme(theme: String) {
        scope.launch(Dispatchers.Main) {
            onThemeChanged.emit(theme)
        }
    }
}
