import Foundation
import UIKit
import SwiftUI
import Orchard

/// Shared state for system status bar visibility, observed by the host app.
///
/// `ObservableObject` with `@Published` so SwiftUI views can react to changes.
/// The host view controller typically reads `isStatusBarHidden` in its
/// `prefersStatusBarHidden` override.
@MainActor
public final class SystemUIState: ObservableObject {
    public static let shared = SystemUIState()
    
    @Published public var isStatusBarHidden: Bool = false
    
    private init() {}
}

/// Controls status bar visibility for immersive experiences.
///
/// Animates the transition for a polished feel. Unlike Android, iOS has no
/// system navigation bar to toggle -- the home indicator is always managed
/// by the system.
public final class SystemBarsCommand: BridgeCommand {
    public let action = "systemBars"

    public init() {}

    @MainActor
    public func handle(content: [String: Any]?) async throws -> [String: Any]? {
        guard let content = content, let showStatusBar = content["showStatusBar"] as? Bool else {
            throw BridgeError.invalidParameter("showStatusBar")
        }
        
        Orchard.v("[Bridge] System bars command: showStatusBar=\(showStatusBar)")
        
        withAnimation(.easeInOut(duration: 0.2)) {
            SystemUIState.shared.isStatusBarHidden = !showStatusBar
        }
        return nil
    }
}
