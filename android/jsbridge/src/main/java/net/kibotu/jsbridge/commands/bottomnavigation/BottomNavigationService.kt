package net.kibotu.jsbridge.commands.bottomnavigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow


/**
 * Holds bottom navigation visibility as a [StateFlow].
 *
 * Singleton because visibility is app-wide shared state -- the same bar is
 * shown/hidden regardless of which WebView issues the command. The host
 * Activity collects [isVisible] to drive the bottom bar's actual visibility.
 */
object BottomNavigationService {

    val isVisible: SharedFlow<Boolean>
        field = MutableStateFlow<Boolean>(true)

    fun setVisible(visible: Boolean) {
        isVisible.value = visible
    }

    fun currentVisibility(): Boolean = isVisible.value
}