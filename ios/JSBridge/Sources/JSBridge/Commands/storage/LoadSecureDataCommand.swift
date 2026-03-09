import Foundation

/// Retrieves a value from the iOS Keychain. Returns `NSNull` (not an error) when the key
/// doesn't exist, matching web's `localStorage.getItem()` semantics.
public final class LoadSecureDataCommand: BridgeCommand {
    public let action = "loadSecureData"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let key = content?["key"] as? String else {
            throw BridgeError.invalidParameter("key")
        }
        
        if let value = KeychainHelper.load(key: key) {
            return ["value": value]
        } else {
            return ["value": NSNull()]
        }
    }
}
