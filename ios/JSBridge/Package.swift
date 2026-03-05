// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "JSBridge",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "JSBridge", targets: ["JSBridge"]),
    ],
    dependencies: [
        .package(url: "https://github.com/kibotu/Orchard", from: "1.0.8"),
    ],
    targets: [
        .target(
            name: "JSBridge",
            dependencies: [
                .product(name: "Orchard", package: "orchard"),
            ],
            resources: [
                .copy("Resources/bridge.js"),
            ]
        ),
    ]
)
