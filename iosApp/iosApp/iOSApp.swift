import SwiftUI
import ComposeApp
import Firebase

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        KoinHelperKt.doInitKoin()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
