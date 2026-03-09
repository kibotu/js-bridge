import Foundation

/// Opt-in protocol for commands that need access to the bridge instance.
///
/// The bridge is set automatically by ``JavaScriptBridge/init(webView:viewController:bridgeName:commands:)``
/// after construction, so commands can use no-arg initializers and still access the bridge
/// (or its `webView` / `viewController`) at execution time.
///
/// `@unchecked Sendable` is expected on adopters because the mutable `bridge` property
/// is only accessed on `@MainActor`.
@MainActor
public protocol BridgeAware: AnyObject {
    var bridge: JavaScriptBridge? { get set }
}
