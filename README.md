# Construction Issue Tracker

A Kotlin Multiplatform (KMP) app for managing construction issues across flats. Built with Jetpack Compose Multiplatform for iOS and Android.

## 🎯 Features

### ✅ Implemented (Sessions 1-4)
- **Cross-platform UI**: Same codebase runs on Android & iOS
- **Issue List Screen**: View all construction issues with status colors
- **SQLDelight Database**: Type-safe local persistence
- **Repository Pattern**: Clean architecture with expect/actual for platform-specific code
- **State Management**: ViewModels with StateFlow for reactive UI
- **Material Design 3**: Modern UI components

### 🚧 Coming Soon
- Camera integration for issue photos
- Create/Edit issue screens
- User authentication
- Issue assignment workflow
- Status filtering
- Real-time updates

## 📱 Screenshots

### Android
- Issue list with status badges (OPEN, IN_PROGRESS, FIXED, VERIFIED)
- Material 3 cards with flat numbers and descriptions

### iOS
- Native iOS look and feel
- Same functionality as Android

## 🛠 Tech Stack

- **Kotlin Multiplatform**: Shared business logic
- **Jetpack Compose Multiplatform**: Shared UI
- **SQLDelight**: Type-safe database
- **Kotlinx Coroutines**: Async programming
- **StateFlow**: Reactive state management
- **Kotlinx Serialization**: JSON handling
- **Kotlinx DateTime**: Cross-platform date/time

## 🏗 Architecture
```
├── commonMain/          # Shared code (80% of app)
│   ├── models/          # Data classes (Issue, User)
│   ├── database/        # SQLDelight schema & queries
│   ├── repository/      # Data access layer
│   ├── viewmodel/       # Business logic & state
│   └── ui/              # Compose UI screens
├── androidMain/         # Android-specific (10%)
│   └── database/        # Android SQLite driver
├── iosMain/             # iOS-specific (10%)
│   └── database/        # iOS SQLite driver
```

## 🚀 Getting Started

### Prerequisites
- Android Studio with KMP plugin
- Xcode (for iOS)
- Java JDK 17
- macOS (for iOS development)

### Run Android
```bash
./gradlew :composeApp:installDebug
```

Or click Run in Android Studio

### Run iOS
```bash
cd iosApp
open iosApp.xcodeproj
```

Then click Run in Xcode (Cmd+R)

## 📚 What I Learned

### Session 1: Setup
- KMP project structure
- Gradle configuration
- expect/actual mechanism

### Session 2: Data Layer
- Kotlin syntax vs Swift
- Data classes and enums
- SQLDelight schema design

### Session 3: Platform Abstraction
- Repository pattern
- Database drivers (Android/iOS)
- Dependency injection basics

### Session 4: UI & State
- Jetpack Compose basics
- StateFlow reactive programming
- ViewModel lifecycle
- LazyColumn for lists

## 🎓 Learning Resources

- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)

## 📝 License

Personal learning project - feel free to use as reference!

## 👤 Author

Marek Hajdučák - Learning KMP development