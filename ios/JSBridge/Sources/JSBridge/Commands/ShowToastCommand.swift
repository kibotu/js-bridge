import Foundation
import UIKit

/// Shows a brief, auto-dismissing message using `UIAlertController`.
///
/// iOS has no native toast API, so we present a `UIAlertController` without
/// buttons and dismiss it after a short delay. Not perfect, but consistent
/// across iOS versions without pulling in a third-party library.
///
/// `@unchecked Sendable` because the weak `viewController` ref is only accessed
/// on `@MainActor`.
public final class ShowToastCommand: BridgeCommand, @unchecked Sendable {
    public let action = "showToast"
    
    weak var viewController: UIViewController?
    
    public init(viewController: UIViewController?) {
        self.viewController = viewController
    }
    
    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let message = content?["message"] as? String else {
            throw BridgeError.invalidParameter("message")
        }
        
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        viewController?.present(alert, animated: true)
        
        let duration = (content?["duration"] as? String) == "long" ? 3.5 : 2.0
        DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
            alert.dismiss(animated: true)
        }
        
        return nil
    }
}
