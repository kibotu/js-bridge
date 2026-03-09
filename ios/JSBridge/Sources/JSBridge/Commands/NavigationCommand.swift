import Foundation
import UIKit
import WebKit
import Orchard

/// Handles back navigation, URL navigation, and app exit.
///
/// Back navigation tries three strategies in priority order:
/// 1. WebView history (`webView.goBack()`) -- cheapest, stays in-page
/// 2. UINavigationController pop -- pops the current VC
/// 3. Modal dismiss -- dismisses a presented VC
/// 4. `exit(0)` -- last resort when there's nowhere to go back to
///
/// `@unchecked Sendable` because weak UIKit refs are only accessed on `@MainActor`.
public final class NavigationCommand: BridgeCommand, BridgeAware, @unchecked Sendable {
    public let action = "navigation"

    public weak var bridge: JavaScriptBridge?

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        let urlString = content?["url"] as? String ?? ""
        let isExternal = content?["external"] as? Bool ?? false
        let goBack = content?["goBack"] as? Bool ?? false
        
        Orchard.v("[NavigationCommand] url=\(urlString) external=\(isExternal) goBack=\(goBack)")
        
        if goBack {
            if let webView = bridge?.webView, webView.canGoBack {
                webView.goBack()
                Orchard.v("[NavigationCommand] Navigated back in WebView history")
                return nil
            }
            
            if let navigationController = bridge?.viewController?.navigationController,
               navigationController.viewControllers.count > 1 {
                navigationController.popViewController(animated: true)
                Orchard.v("[NavigationCommand] Popped navigation controller")
                return nil
            }
            
            if let viewController = bridge?.viewController,
               viewController.presentingViewController != nil {
                await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
                    viewController.dismiss(animated: true) {
                        Orchard.v("[NavigationCommand] Dismissed modal view controller")
                        continuation.resume()
                    }
                }
                return nil
            }
            
            Orchard.w("[NavigationCommand] No back navigation available, exiting app")
            exit(0)
        }
        
        if !urlString.isEmpty {
            guard let url = URL(string: urlString) else {
                throw BridgeError.invalidParameter("Invalid URL: \(urlString)")
            }
            
            if isExternal {
                await UIApplication.shared.open(url)
            } else {
                Orchard.v("[NavigationCommand] Internal navigation to: \(urlString)")
                TabNavigationService.shared.switchToTab(1)
            }
            return nil
        }
        
        throw BridgeError.invalidParameter("Missing navigation parameter")
    }
}
