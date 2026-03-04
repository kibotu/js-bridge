package net.kibotu.bridgesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import net.kibotu.bridgesample.bridge.JavaScriptBridge
import net.kibotu.bridgesample.bridge.SafeAreaService
import net.kibotu.bridgesample.bridge.commands.refresh.RefreshService
import net.kibotu.bridgesample.bridge.commands.systembars.isLightNavigationBar
import net.kibotu.bridgesample.bridge.commands.systembars.isLightStatusBar
import net.kibotu.bridgesample.misc.weak
import net.kibotu.bridgesample.ui.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private var currentBridge: JavaScriptBridge? by weak()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        window?.isLightNavigationBar = true
        window?.isLightStatusBar = true

        super.onCreate(savedInstanceState)

        setContent {
            Screen(
                onBackPressed = {
                    onBackPressedDispatcher.onBackPressed()
                },
                onBridgeReady = { currentBridge = it }
            )
        }

        // random events emulating pushes
        lifecycleScope.launch {
            while (true) {
                delay(Random.nextLong(7000, 15000))
                currentBridge?.sendToWeb(
                    "onPushNotification",
                    mapOf(
                        "url" to "https://www.google.com",
                        "message" to "Lorem Ipsum"
                    )
                )
            }
        }

        // listen to when web wants to refresh native
        lifecycleScope.launch {
            RefreshService.onRefresh.collect {
                Timber.v("refreshing $it")
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val event = if (hasFocus) "focused" else "defocused"
        currentBridge?.sendToWeb("lifecycle", mapOf("event" to event))
        if (hasFocus) {
            SafeAreaService.pushTobridge(currentBridge)
        }
    }
}

