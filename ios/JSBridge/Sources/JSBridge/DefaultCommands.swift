import Foundation
import UIKit
import WebKit

/// Convenience factory providing all built-in bridge commands.
///
/// Commands are **not** registered automatically. This helper lets
/// an app opt-in to all defaults with a single call:
///
/// ```swift
/// let bridge = JavaScriptBridge(
///     webView: webView,
///     viewController: self,
///     commands: DefaultCommands.all(
///         viewController: self,
///         webView: webView,
///         bridge: bridge
///     )
/// )
/// ```
public struct DefaultCommands {

    /// Returns all built-in commands.
    ///
    /// - Parameters:
    ///   - viewController: The hosting view controller (needed by UI commands like alerts/toasts).
    ///   - webView: The WKWebView (needed by navigation commands).
    ///   - bridge: Optional bridge reference for commands that push safe-area updates.
    public static func all(
        viewController: UIViewController? = nil,
        webView: WKWebView? = nil,
        bridge: JavaScriptBridge? = nil
    ) -> [BridgeCommand] {
        return [
            // Device & System
            DeviceInfoHandler(),
            NetworkStatusHandler(),
            OpenSettingsHandler(),

            // UI
            ShowToastHandler(viewController: viewController),
            ShowAlertHandler(viewController: viewController),

            // Navigation
            TopNavigationHandler(bridge: bridge),
            BottomNavigationHandler(bridge: bridge),
            NavigationHandler(viewController: viewController, webView: webView),
            OpenUrlHandler(),

            // System
            SystemBarsHandler(),
            GetInsetsHandler(viewController: viewController),
            HapticHandler(),
            CopyToClipboardHandler(),
            RequestPermissionsHandler(),

            // Storage
            SaveSecureDataHandler(),
            LoadSecureDataHandler(),
            RemoveSecureDataHandler(),

            // Analytics
            TrackEventHandler(),
            TrackScreenHandler(),

            // Theme
            ThemeChangedHandler(),

            // Refresh
            RefreshHandler(),
        ]
    }
}
