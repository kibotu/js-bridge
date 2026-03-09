import Foundation

/// Toggles bottom navigation bar visibility from web content.
///
/// Pushes updated safe area insets after the change so web content can
/// reclaim (or yield) the space immediately.
///
/// `@unchecked Sendable` because the weak `bridge` ref can't satisfy
/// the compiler's strict sendability check, but it's safe -- we only
/// access it on `@MainActor`.
public final class BottomNavigationCommand: BridgeCommand, @unchecked Sendable {
    public let action = "bottomNavigation"

    weak var bridge: JavaScriptBridge?

    public init(bridge: JavaScriptBridge? = nil) {
        self.bridge = bridge
    }

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let isVisible = content?["isVisible"] as? Bool else {
            throw BridgeError.invalidParameter("isVisible")
        }

        BottomNavigationService.shared.setVisible(isVisible)
        SafeAreaService.shared.pushToBridge(bridge)
        return nil
    }
}
