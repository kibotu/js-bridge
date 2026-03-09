package net.kibotu.jsbridge.commands

import net.kibotu.jsbridge.JavaScriptBridge

/**
 * Opt-in interface for commands that need access to the [JavaScriptBridge] instance.
 *
 * The bridge is set automatically by [JavaScriptBridge.inject] after construction,
 * so commands can use no-arg constructors and still access the bridge (or its context)
 * at execution time.
 */
interface BridgeAware {
    var bridge: JavaScriptBridge?
}
