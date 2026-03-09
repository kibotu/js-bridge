# JSBridge for iOS

[![iOS CI](https://github.com/kibotu/js-bridge/actions/workflows/ios.yml/badge.svg)](https://github.com/kibotu/js-bridge/actions/workflows/ios.yml)

A Swift package that gives your WKWebView a promise-based JavaScript bridge. Web content calls native with `await jsbridge.call(...)`, native pushes events back with `bridge.sendToWeb(...)`. One API, no ceremony.

## Requirements

- iOS 16+
- Swift 6.0+
- Xcode 16+

## Installation

### Swift Package Manager (remote)

```swift
.package(url: "https://github.com/kibotu/js-bridge", from: "1.0.0")
```

Then add the product to your target:

```swift
.product(name: "JSBridge", package: "js-bridge")
```

### Swift Package Manager (local)

If you've cloned the repo:

```swift
.package(path: "../path/to/js-bridge")
```

### Xcode

File → Add Package Dependencies → paste `https://github.com/kibotu/js-bridge` → Add Package.

## Quick Start

```swift
import JSBridge

let bridge = JavaScriptBridge(
    webView: webView,
    viewController: self,
    commands: DefaultCommands.all(viewController: self, webView: webView)
)
```

That's the whole setup. Your web content can now call native:

```js
const info = await jsbridge.call('deviceInfo');
```

## Usage

### Pick Your Commands

Use all defaults, or only what you need:

```swift
// Everything
let bridge = JavaScriptBridge(
    webView: webView,
    viewController: self,
    commands: DefaultCommands.all(viewController: self, webView: webView)
)

// Just the essentials
let bridge = JavaScriptBridge(
    webView: webView,
    viewController: self,
    commands: [
        DeviceInfoCommand(),
        ShowToastCommand(viewController: self),
        HapticCommand(),
    ]
)
```

### Send Events to Web

```swift
bridge.sendToWeb(action: "lifecycle", content: ["event": "focused"])
```

### Custom Bridge Name

The default global is `window.jsbridge`. Change it if you need to:

```swift
let bridge = JavaScriptBridge(
    webView: webView,
    viewController: self,
    bridgeName: "myApp",
    commands: DefaultCommands.all(viewController: self, webView: webView)
)
```

Web code then uses `await myApp.call(...)` instead.

### Multiple Bridges

Register separate bridges on the same WebView for different concerns:

```swift
let mainBridge = JavaScriptBridge(
    webView: webView, viewController: self, bridgeName: "jsbridge",
    commands: [DeviceInfoCommand(), ShowToastCommand(viewController: self)]
)
let analyticsBridge = JavaScriptBridge(
    webView: webView, viewController: self, bridgeName: "analytics",
    commands: [TrackEventCommand(), TrackScreenCommand()]
)
```

## Adding a Custom Command

Implement `BridgeCommand`. That's the whole contract:

```swift
class MyCommand: BridgeCommand {
    let action = "myAction"

    func handle(content: [String: Any]?) async throws -> [String: Any]? {
        let name = content?["name"] as? String ?? "world"
        return ["greeting": "hello, \(name)"]
    }
}
```

Register it:

```swift
let bridge = JavaScriptBridge(
    webView: webView,
    viewController: self,
    commands: DefaultCommands.all(viewController: self, webView: webView) + [MyCommand()]
)
```

Call it from web:

```js
const result = await jsbridge.call('myAction', { name: 'Swift' });
// → { greeting: "hello, Swift" }
```

Throw `BridgeError` for expected failures:

```swift
func handle(content: [String: Any]?) async throws -> [String: Any]? {
    guard let id = content?["id"] as? String else {
        throw BridgeError.invalidParameter("id")
    }
    // ...
}
```

Return `nil` for fire-and-forget commands that don't need a response.

## Lifecycle & Focus

Web content has no way to know it's been covered by a modal, buried in a tab, or backgrounded. JSBridge includes a `WindowFocusObserver` protocol that handles this automatically.

Conform your view controller:

```swift
class WebViewController: UIViewController, WindowFocusObserver {

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        windowFocusDidAppear()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        windowFocusWillDisappear()
    }

    func onWindowFocusChanged(hasFocus: Bool) {
        if hasFocus {
            bridge.sendToWeb(action: "lifecycle", content: ["event": "focused"])
            SafeAreaService.shared.pushToBridge(bridge)
        } else {
            bridge.sendToWeb(action: "lifecycle", content: ["event": "defocused"])
        }
    }
}
```

This covers modals, tab switches, background/foreground, and navigation stack changes.

## Safe Area

Native automatically pushes CSS custom properties whenever bars change, the device rotates, or the screen regains focus. Web content doesn't poll -- it just uses CSS:

```css
body {
  padding-top: var(--bridge-inset-top, env(safe-area-inset-top, 0px));
  padding-bottom: var(--bridge-inset-bottom, env(safe-area-inset-bottom, 0px));
}
```

Programmatic push:

```swift
SafeAreaService.shared.pushToBridge(bridge)
```

## SwiftUI

Wrap the bridge in a `UIViewControllerRepresentable`. The sample app shows the full pattern:

```swift
struct WebViewContainer: UIViewControllerRepresentable {
    let url: URL
    let onBridgeReady: (JavaScriptBridge) -> Void

    func makeUIViewController(context: Context) -> WebViewController {
        let controller = WebViewController()
        controller.url = url
        controller.onBridgeReady = onBridgeReady
        return controller
    }

    func updateUIViewController(_ controller: WebViewController, context: Context) {}
}
```

## Built-in Commands

| Action | Description |
|--------|-------------|
| `deviceInfo` | Platform, OS version, model, app version |
| `networkState` | Connectivity status and type |
| `openSettings` | Opens the app's Settings page |
| `getInsets` | Current safe area insets |
| `showToast` | Native toast message |
| `showAlert` | Native alert dialog |
| `topNavigation` | Show/hide/configure the top navigation bar |
| `bottomNavigation` | Show/hide the bottom tab bar |
| `systemBars` | Status bar and system navigation (Android-specific, no-op on iOS) |
| `haptic` | Haptic feedback |
| `navigation` | Load URLs or go back |
| `copyToClipboard` | Copy text to clipboard |
| `requestPermissions` | Request runtime permissions |
| `saveSecureData` | Store data in Keychain |
| `loadSecureData` | Read data from Keychain |
| `removeSecureData` | Delete data from Keychain |
| `trackEvent` | Analytics event (fire-and-forget) |
| `trackScreen` | Analytics screen view (fire-and-forget) |
| `themeChanged` | Respond to theme/appearance changes |
| `refresh` | Trigger a page refresh |

## Building the Sample App

```bash
open ios/BridgeSample.xcodeproj
```

The Xcode project already has JSBridge configured as a local SPM dependency. Hit Cmd+R. The sample app loads `index.html` with all bridge features wired up.

## Debugging

Connect your device, then open Safari → Develop → your device → your WebView:

```js
await jsbridge.call('deviceInfo')
jsbridge.setDebug(true)    // logs every message
jsbridge.getStats()        // bridge state snapshot
```

## Project Structure

```
ios/
├── JSBridge/                    # Swift Package
│   ├── Package.swift
│   └── Sources/JSBridge/
│       ├── JavaScriptBridge.swift       # Core bridge
│       ├── BridgeCommand.swift          # Command protocol
│       ├── DefaultCommands.swift        # All built-in commands
│       ├── SafeAreaService.swift        # CSS custom property injection
│       ├── WindowFocusObserver.swift    # Focus lifecycle protocol
│       ├── Commands/                    # One file per action
│       └── Resources/bridge.js         # Injected JavaScript
└── BridgeSample/                # Sample app
    └── Views/
```

## License

See [LICENSE](../LICENSE).
