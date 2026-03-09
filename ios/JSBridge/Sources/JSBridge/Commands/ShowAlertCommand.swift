import Foundation
import UIKit

/// Presents a native `UIAlertController` so web content can show platform-consistent dialogs.
///
/// `@unchecked Sendable` because the weak `bridge` ref is only accessed
/// on `@MainActor`.
public final class ShowAlertCommand: BridgeCommand, BridgeAware, @unchecked Sendable {
    public let action = "showAlert"

    public weak var bridge: JavaScriptBridge?

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let title = content?["title"] as? String,
              let message = content?["message"] as? String else {
            throw BridgeError.invalidParameter("title or message")
        }
        
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        if let buttons = content?["buttons"] as? [String] {
            for buttonTitle in buttons {
                alert.addAction(UIAlertAction(title: buttonTitle, style: .default))
            }
        } else {
            alert.addAction(UIAlertAction(title: "OK", style: .default))
        }
        
        bridge?.viewController?.present(alert, animated: true)
        return nil
    }
}
