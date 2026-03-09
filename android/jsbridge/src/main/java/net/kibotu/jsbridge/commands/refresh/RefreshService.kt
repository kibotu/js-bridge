package net.kibotu.jsbridge.commands.refresh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.kibotu.jsbridge.commands.refresh.RefreshService.onRefresh

/**
 * Broadcasts refresh events triggered by web content.
 *
 * Uses [SharedFlow] because a refresh is a one-shot event, not persistent state --
 * a new collector should not automatically replay the last refresh. The host app
 * collects [onRefresh] to reload data, re-fetch configs, etc.
 */
object RefreshService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val onRefresh: SharedFlow<String?>
        field = MutableSharedFlow<String?>()

    fun refresh(url: String?) {
        scope.launch(Dispatchers.Main) {
            onRefresh.emit(url)
        }
    }
}
