import SwiftUI
import Orchard
import JSBridge

/// Main tab view with bottom navigation
struct MainTabView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @State private var selectedTab = 0
    @State private var currentBridge: JavaScriptBridge?
    @ObservedObject private var bottomNavService = BottomNavigationService.shared
    @ObservedObject private var topNavService = TopNavigationService.shared
    @ObservedObject private var systemUIState = SystemUIState.shared
    @ObservedObject private var themeService = ThemeService.shared

    var body: some View {
        TabView(selection: $selectedTab) {
            homeTab
                .tag(0)
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }

            webTab
                .tag(1)
                .tabItem {
                    Label("Web", systemImage: "globe")
                }
        }
        .accentColor(.accentBlue)
        .statusBar(hidden: systemUIState.isStatusBarHidden)
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            currentBridge?.notifyLifecycleEvent("focused")
            SafeAreaService.shared.pushToBridge(currentBridge)
        }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
            currentBridge?.notifyLifecycleEvent("defocused")
        }
        .onReceive(themeManager.$isDarkMode.dropFirst()) { isDark in
            currentBridge?.sendToWeb(action: "themeChanged", content: [
                "theme": isDark ? "dark" : "light"
            ])
        }
        .onReceive(themeService.$requestedTheme.dropFirst()) { theme in
            themeManager.isDarkMode = (theme == "dark")
        }
    }

    // MARK: - Tabs

    private var homeTab: some View {
        VStack(spacing: 0) {
            TopNavigationView(onBackPressed: {
                Orchard.v("[MainTabView] Back button pressed")
            })

            WebViewScreen(
                url: getLocalFileURL(filename: "index.html"),
                onBridgeReady: { bridge in
                    currentBridge = bridge
                    Orchard.v("[MainTabView] Bridge ready for Tab 1")
                    startPushNotificationSimulation(bridge: bridge)
                },
                shouldRespectTopSafeArea: !topNavService.config.isVisible,
                shouldRespectBottomSafeArea: !bottomNavService.config.isVisible
            )
        }
        .background(Color.slateBackground)
        .ignoresSafeArea(.all, edges: .all)
        .tabBarHidden(!bottomNavService.config.isVisible, animated: true)
    }

    private var webTab: some View {
        VStack(spacing: 0) {
            TopNavigationView(onBackPressed: {
                Orchard.v("[MainTabView] Back button pressed")
            })

            WebViewScreen(
                url: URL(string: "https://trail.services.kibotu.net/")!,
                onBridgeReady: { bridge in
                    currentBridge = bridge
                    Orchard.v("[MainTabView] Bridge ready for Tab 2")
                },
                shouldRespectTopSafeArea: !topNavService.config.isVisible,
                shouldRespectBottomSafeArea: !bottomNavService.config.isVisible
            )
        }
        .background(Color.slateBackground)
        .ignoresSafeArea(.all, edges: .all)
        .tabBarHidden(!bottomNavService.config.isVisible, animated: true)
    }

    // MARK: - Helpers

    private func getLocalFileURL(filename: String) -> URL {
        if let url = Bundle.main.url(forResource: filename.replacingOccurrences(of: ".html", with: ""), withExtension: "html", subdirectory: "Resources") {
            return url
        }
        return Bundle.main.url(forResource: filename.replacingOccurrences(of: ".html", with: ""), withExtension: "html") ?? URL(string: "about:blank")!
    }

    private func startPushNotificationSimulation(bridge: JavaScriptBridge) {
        Timer.scheduledTimer(withTimeInterval: Double.random(in: 7...15), repeats: true) { _ in
            bridge.sendToWeb(action: "onPushNotification", content: [
                "url": "https://www.google.com",
                "message": "Lorem Ipsum"
            ])
        }
    }
}

// MARK: - Tab Bar Visibility Modifier

private struct TabBarHiddenModifier: ViewModifier {
    let isHidden: Bool
    let animated: Bool

    func body(content: Content) -> some View {
        if #available(iOS 16.0, *) {
            content
                .toolbar(isHidden ? .hidden : .visible, for: .tabBar)
                .animation(animated ? .easeInOut(duration: 0.3) : nil, value: isHidden)
        } else {
            content
                .onAppear { setTabBarHidden(isHidden) }
                .onChange(of: isHidden) { setTabBarHidden($0) }
        }
    }

    private func setTabBarHidden(_ hidden: Bool) {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene }).first,
              let window = windowScene.windows.first,
              let tabBarController = findTabBarController(from: window.rootViewController)
        else { return }

        let tabBar = tabBarController.tabBar
        let tabBarHeight = tabBar.frame.height

        guard animated else {
            tabBar.isHidden = hidden
            if let containerView = tabBarController.view {
                containerView.frame.size.height = window.frame.height + (hidden ? tabBarHeight : -tabBarHeight)
            }
            return
        }

        if hidden {
            UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseInOut) {
                tabBar.alpha = 0
                tabBar.frame.origin.y = window.frame.maxY
            } completion: { _ in
                tabBar.isHidden = true
            }
        } else {
            tabBar.isHidden = false
            tabBar.alpha = 0
            tabBar.frame.origin.y = window.frame.maxY
            UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseInOut) {
                tabBar.alpha = 1
                tabBar.frame.origin.y = window.frame.maxY - tabBarHeight
            }
        }
    }

    private func findTabBarController(from vc: UIViewController?) -> UITabBarController? {
        if let tbc = vc as? UITabBarController { return tbc }
        for child in vc?.children ?? [] {
            if let found = findTabBarController(from: child) { return found }
        }
        if let presented = vc?.presentedViewController {
            return findTabBarController(from: presented)
        }
        return nil
    }
}

extension View {
    func tabBarHidden(_ hidden: Bool, animated: Bool = true) -> some View {
        modifier(TabBarHiddenModifier(isHidden: hidden, animated: animated))
    }
}
