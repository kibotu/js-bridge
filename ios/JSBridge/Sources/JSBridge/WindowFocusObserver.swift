import UIKit

/// Marker protocol for view controllers that want automatic window focus
/// change detection -- analogous to Android's `Activity.onWindowFocusChanged`.
///
/// **This protocol is a pure UIKit concern.** It has no dependency on
/// `JavaScriptBridge` or any bridge-specific types. Conformers decide
/// what to do when focus changes (e.g. forward to a bridge, pause
/// animations, fire analytics, …).
///
/// ## Adoption
///
/// 1. Call `windowFocusDidAppear()` from `viewDidAppear(_:)`.
/// 2. Call `windowFocusWillDisappear()` from `viewWillDisappear(_:)`.
/// 3. Implement `onWindowFocusChanged(hasFocus:)`.
///
/// The protocol extension handles the rest: 60 Hz polling for modal
/// occlusion, app-lifecycle notifications, and deduplication.
public protocol WindowFocusObserver: UIViewController {

    /// Whether this view controller wants focus monitoring.
    /// Default is `true`. Return `false` to opt out.
    var wantsToListenOnFocusEvents: Bool { get }

    /// Called exactly once per state transition. Implement this to react
    /// to focus changes — there is intentionally no default implementation.
    func onWindowFocusChanged(hasFocus: Bool)
}
