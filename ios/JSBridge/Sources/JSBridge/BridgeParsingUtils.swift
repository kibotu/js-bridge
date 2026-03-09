import Foundation

/// Typed extraction helpers for bridge command content dictionaries.
/// Mirrors Android's BridgeParsingUtils for consistent parameter parsing across platforms.
extension Dictionary where Key == String, Value == Any {
    public func string(_ key: String) -> String? {
        self[key] as? String
    }

    public func int(_ key: String) -> Int? {
        self[key] as? Int
    }

    public func double(_ key: String) -> Double? {
        self[key] as? Double
    }

    public func bool(_ key: String) -> Bool? {
        self[key] as? Bool
    }

    public func stringArray(_ key: String) -> [String]? {
        self[key] as? [String]
    }

    public func dict(_ key: String) -> [String: Any]? {
        self[key] as? [String: Any]
    }

    public func requireString(_ key: String) throws -> String {
        guard let value = self[key] as? String, !value.isEmpty else {
            throw BridgeError.invalidParameter(key)
        }
        return value
    }

    public func requireBool(_ key: String) throws -> Bool {
        guard let value = self[key] as? Bool else {
            throw BridgeError.invalidParameter(key)
        }
        return value
    }

    public func requireStringArray(_ key: String) throws -> [String] {
        guard let value = self[key] as? [String], !value.isEmpty else {
            throw BridgeError.invalidParameter(key)
        }
        return value
    }
}
