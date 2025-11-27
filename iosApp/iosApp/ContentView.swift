import SwiftUI
import ComposeApp

struct ContentView: View {
    var body: some View {
        ComposeViewControllerWrapper()
            .ignoresSafeArea()
    }
}

struct ComposeViewControllerWrapper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.createViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Nothing to update
    }
}