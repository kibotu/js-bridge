import Foundation
import SwiftUI
import Orchard

/// Service to manage tab navigation across the app
public class TabNavigationService: ObservableObject {
    @Published public var selectedTab: Int = 0

    public static let shared = TabNavigationService()

    private init() {}

    public func switchToTab(_ index: Int) {
        DispatchQueue.main.async {
            withAnimation {
                self.selectedTab = index
            }
            Orchard.v("[TabNavigationService] Switched to tab \(index)")
        }
    }
}
