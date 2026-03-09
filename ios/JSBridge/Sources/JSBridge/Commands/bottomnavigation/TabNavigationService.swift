import Foundation
import SwiftUI
import Orchard

/// Drives programmatic tab switching from bridge commands.
///
/// Separate from `BottomNavigationService` because tab *selection* and
/// tab bar *visibility* are independent concerns -- you might hide the bar
/// but still switch tabs programmatically (e.g., deep links).
@MainActor
public final class TabNavigationService: ObservableObject {
    @Published public var selectedTab: Int = 0

    public static let shared = TabNavigationService()

    private init() {}

    public func switchToTab(_ index: Int) {
        withAnimation {
            self.selectedTab = index
        }
        Orchard.v("[TabNavigationService] Switched to tab \(index)")
    }
}
