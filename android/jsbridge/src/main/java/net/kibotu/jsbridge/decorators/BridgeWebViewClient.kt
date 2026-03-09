package net.kibotu.jsbridge.decorators

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import net.kibotu.jsbridge.JavaScriptBridge.Companion.bridge
import net.kibotu.jsbridge.SafeAreaService
import timber.log.Timber

/**
 * [WebViewClientDecorator] that wires up the bridge lifecycle automatically.
 *
 * - **onPageStarted**: Injects `bridge.js` early so the script is available before
 *   the page's own JavaScript runs. This must happen on every navigation because
 *   WebView discards injected scripts on page transitions.
 * - **onPageFinished**: Pushes safe area CSS once the DOM is ready to receive it.
 */
class BridgeWebViewClient(delegate: WebViewClient?) : WebViewClientDecorator(delegate) {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val bridge = view?.bridge() ?: return
        bridge.injectBridgeScript()
        Timber.d("[BridgeWebViewClient] Bridge injected for: $url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val bridge = view?.bridge() ?: return
        SafeAreaService.pushTobridge(bridge)
    }
}
