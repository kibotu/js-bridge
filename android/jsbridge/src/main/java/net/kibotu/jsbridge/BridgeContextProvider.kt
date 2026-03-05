package net.kibotu.jsbridge

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper

/**
 * Resolves [Activity] and [Application] from a [Context] without relying on
 * global singletons like `ApplicationProvider`.
 *
 * Typical usage: pass `webView.context` — the framework will unwrap any
 * [ContextWrapper] chain until it finds the hosting [Activity].
 */
object BridgeContextProvider {

    fun findActivity(context: Context?): Activity? {
        var ctx = context
        while (ctx != null) {
            if (ctx is Activity) return ctx
            ctx = (ctx as? ContextWrapper)?.baseContext
        }
        return null
    }

    fun findApplication(context: Context?): Application? {
        return context?.applicationContext as? Application
    }
}
