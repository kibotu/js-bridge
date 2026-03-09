import Foundation

/// Deletes a value from the iOS Keychain. Useful for logout flows and privacy compliance.
public final class RemoveSecureDataCommand: BridgeCommand {
    public let action = "removeSecureData"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let key = content?["key"] as? String else {
            throw BridgeError.invalidParameter("key")
        }
        
        guard KeychainHelper.delete(key: key) else {
            throw BridgeError.internalError("Failed to remove from keychain")
        }
        return nil
    }
}
