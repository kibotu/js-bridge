import Foundation
import UIKit

/// Command for device information requests
public final class DeviceInfoCommand: BridgeCommand {
    public let action = "deviceInfo"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        let osVersion = UIDevice.current.systemVersion
        return [
            "platform": "iOS",
            "osVersion": osVersion,
            "sdkVersion": getSDKVersion(fallback: osVersion),
            "manufacturer": "Apple",
            "model": getDeviceIdentifier(),
        ]
    }
    
    nonisolated private func getSDKVersion(fallback: String) -> String {
        if let deploymentTarget = Bundle.main.infoDictionary?["MinimumOSVersion"] as? String {
            return deploymentTarget
        }
        return fallback
    }
    
    nonisolated private func getDeviceIdentifier() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        return identifier
    }
}
