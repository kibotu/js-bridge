import Foundation

/// Placeholder for app-wide refresh triggered by web content.
///
/// No-op today. The Android counterpart wires through `RefreshService` --
/// this should follow suit once the host app needs refresh coordination.
public final class RefreshCommand: BridgeCommand {
    public let action = "refresh"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        return nil
    }
}
