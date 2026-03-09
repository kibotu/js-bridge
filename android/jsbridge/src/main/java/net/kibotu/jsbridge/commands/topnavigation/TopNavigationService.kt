package net.kibotu.jsbridge.commands.topnavigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the current top navigation configuration as a [StateFlow].
 *
 * [StateFlow] (not [SharedFlow]) because the config represents state -- there is
 * always a current value and new collectors should receive it immediately. The host
 * Activity observes [config] to render the native toolbar accordingly.
 */
object TopNavigationService {

    val config: SharedFlow<TopNavigationConfig>
        field = MutableStateFlow<TopNavigationConfig>(TopNavigationConfig())

    fun applyConfig(newConfig: TopNavigationConfig) {
        config.value = newConfig
    }

    fun currentConfig(): TopNavigationConfig = config.value
}