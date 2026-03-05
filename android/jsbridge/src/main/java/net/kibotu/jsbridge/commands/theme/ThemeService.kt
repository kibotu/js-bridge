package net.kibotu.jsbridge.commands.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

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
