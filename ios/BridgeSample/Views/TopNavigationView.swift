import SwiftUI
import JSBridge

/// Top navigation bar view (sample app specific, uses ThemeManager)
struct TopNavigationView: View {
    @ObservedObject var service = TopNavigationService.shared
    @EnvironmentObject var themeManager: ThemeManager
    let onBackPressed: () -> Void

    private var topSafeAreaInset: CGFloat {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows.first?.safeAreaInsets.top ?? 0
    }

    var body: some View {
        if service.config.isVisible {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    if service.config.showBackButton {
                        Button(action: onBackPressed) {
                            Image(systemName: "chevron.left")
                                .foregroundColor(.primary)
                                .font(.system(size: 20, weight: .medium))
                        }
                    }

                    if service.config.showLogo {
                        Image(systemName: "app.fill")
                            .foregroundColor(.accentBlue)
                            .font(.system(size: 24))
                    } else if let title = service.config.title {
                        Text(title)
                            .font(.headline)
                            .fontWeight(.semibold)
                    }

                    Spacer()

                    Button(action: { themeManager.toggle() }) {
                        Image(systemName: themeManager.isDarkMode ? "sun.max.fill" : "moon.fill")
                            .foregroundColor(.accentBlue)
                            .font(.system(size: 20))
                    }

                    if service.config.showProfileIconWidget {
                        Button(action: {}) {
                            Image(systemName: "person.circle.fill")
                                .foregroundColor(.secondary)
                                .font(.system(size: 28))
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .padding(.top, topSafeAreaInset)
                .background(Color.surfaceColor)

                if service.config.showDivider {
                    Divider()
                }
            }
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }
}
