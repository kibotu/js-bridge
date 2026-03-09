import Foundation
import UIKit

/// Returns current system inset values so web content can adapt its layout.
///
/// Falls back from the view controller's window scene to the first connected scene
/// to handle edge cases where the VC's window is not yet in the hierarchy.
///
/// `@unchecked Sendable` because the weak `bridge` ref is only accessed
/// on `@MainActor`.
public final class GetInsetsCommand: BridgeCommand, BridgeAware, @unchecked Sendable {
    public let action = "insets"

    public weak var bridge: JavaScriptBridge?

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let vc = bridge?.viewController else {
            throw BridgeError.internalError("No view controller")
        }

        let windowScene = vc.view.window?.windowScene
            ?? UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene }).first

        let statusBarHeight = windowScene?.statusBarManager?.statusBarFrame.height ?? 0
        let isStatusBarHidden = windowScene?.statusBarManager?.isStatusBarHidden ?? false
        let rootSafeArea = vc.view.window?.safeAreaInsets ?? vc.view.safeAreaInsets

        return [
            "statusBar": [
                "height": Int(statusBarHeight),
                "visible": !isStatusBarHidden
            ],
            "systemNavigation": [
                "height": Int(rootSafeArea.bottom),
                "visible": rootSafeArea.bottom > 0
            ],
            "keyboard": [
                "height": 0,
                "visible": false
            ],
            "safeArea": [
                "top": Int(rootSafeArea.top),
                "right": Int(rootSafeArea.right),
                "bottom": Int(rootSafeArea.bottom),
                "left": Int(rootSafeArea.left)
            ]
        ]
    }
}
