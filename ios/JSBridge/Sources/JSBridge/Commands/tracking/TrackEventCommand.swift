import Foundation
import Orchard

/// Forwards analytics events from web content to the native logging layer (fire-and-forget).
///
/// Returns `nil` so no response is sent -- analytics failures should never block user actions.
public final class TrackEventCommand: BridgeCommand {
    public let action = "trackEvent"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let event = content?["event"] as? String else {
            throw BridgeError.invalidParameter("event")
        }
        
        let params = content?["params"] as? [String: Any] ?? [:]
        
        let trackingEvent = BridgeTrackingEvent(name: event, parameters: params)
        Orchard.v("\(trackingEvent)")
        Orchard.v("[Bridge] Track event: \(event) with params: \(params)")
        
        return nil
    }
}

private struct BridgeTrackingEvent: TrackingEvent {
    let name: String
    let parameters: [String: Any]
}

/// Common shape for analytics events. Adopt in your app's tracking adapter to
/// forward bridge events to Firebase, Amplitude, or whatever you use.
public protocol TrackingEvent {
    var name: String { get }
    var parameters: [String: Any] { get }
}
