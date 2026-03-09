import Foundation

/// Configures the native top navigation bar from web content.
///
/// After applying the config, pushes updated safe area insets so web content
/// can adjust layout immediately (e.g., extending behind the status bar
/// when the bar is hidden).
///
/// Note: the web parameter `showUpArrow` maps to `showBackButton` on the native side.
///
/// `@unchecked Sendable` because the weak `bridge` ref can't satisfy
/// strict sendability, but access is confined to `@MainActor`.
public final class TopNavigationCommand: BridgeCommand, @unchecked Sendable {
    public let action = "topNavigation"

    weak var bridge: JavaScriptBridge?

    public init(bridge: JavaScriptBridge? = nil) {
        self.bridge = bridge
    }

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let content = content else {
            throw BridgeError.invalidParameter("content")
        }

        TopNavigationService.shared.update(
            isVisible: content["isVisible"] as? Bool,
            title: content["title"] as? String,
            showBackButton: content["showUpArrow"] as? Bool,
            showDivider: content["showDivider"] as? Bool,
            showLogo: content["showLogo"] as? Bool,
            showProfileIconWidget: content["showProfileIconWidget"] as? Bool
        )

        SafeAreaService.shared.pushToBridge(bridge)
        return nil
    }
}
