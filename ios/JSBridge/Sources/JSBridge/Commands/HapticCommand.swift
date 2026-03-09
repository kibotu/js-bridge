import Foundation
import UIKit

/// Triggers haptic feedback so web content can provide tactile responses.
///
/// Uses `.medium` impact style as a sensible default -- perceptible without being
/// aggressive. Mirrors the Android `HapticCommand` for cross-platform parity.
public final class HapticCommand: BridgeCommand {
    public let action = "haptic"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let vibrate = content?["vibrate"] as? Bool, vibrate else {
            return nil
        }
        
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()
        return nil
    }
}
