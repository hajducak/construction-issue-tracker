import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let databaseDriverFactory = DatabaseDriverFactory()
        let imagePicker = ImagePicker()
        return MainViewControllerKt.MainViewController(
            databaseDriverFactory: databaseDriverFactory,
            imagePicker: imagePicker
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
