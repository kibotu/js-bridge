import SwiftUI

/// Snapshot of bottom navigation bar state, observed by the host app's SwiftUI views.
public struct BottomNavigationConfig: Sendable {
    public var isVisible: Bool = true

    public init(isVisible: Bool = true) {
        self.isVisible = isVisible
    }
}

/// Holds bottom bar visibility as an `ObservableObject` for SwiftUI integration.
///
/// Singleton because bottom bar visibility is app-wide shared state -- the same
/// bar is shown or hidden regardless of which WebView issues the command.
@MainActor
public final class BottomNavigationService: ObservableObject {
    @Published public var config = BottomNavigationConfig()

    public static let shared = BottomNavigationService()

    private init() {}

    public func configure(with config: BottomNavigationConfig) {
        self.config = config
    }

    public func setVisible(_ isVisible: Bool) {
        withAnimation(.easeInOut(duration: 0.3)) {
            self.config.isVisible = isVisible
        }
    }
}
