# Construction Issue Tracker

A Kotlin Multiplatform (KMP) app for managing construction issues across flats. Built with Jetpack Compose Multiplatform for iOS and Android.

## 🎯 Features

### ✅ Implemented (Sessions 1-8)
- **Cross-platform architecture**: ~75% code sharing between Android and iOS
- **Issue management**: Create, view, and track construction issues
- **Photo capture** (Android): CameraX integration with full permissions
- **Photo display**: Thumbnails in list, full-screen viewer on click
- **Issue details**: Dedicated detail screen for each issue
- **Status management**: Update issue status with confirmation dialog
- **User feedback**: Toast notifications for status changes
- **Local database**: SQLDelight with type-safe queries
- **State management**: ViewModels with StateFlow
- **Navigation**: Multi-screen flow (List → Detail → Create)
- **Material Design 3**: Modern, polished UI

### Session Progress
- Session 1: ✅ Environment setup (Android Studio, Xcode, Git)
- Session 2: ✅ Data models & SQLDelight schema
- Session 3: ✅ Repository pattern & expect/actual
- Session 4: ✅ Issue list UI (Android + iOS)
- Session 5: ✅ Create issue screen with navigation
- Session 6: ✅ Real camera integration (Android)
- Session 7: ✅ Photo display with Coil & full-screen viewer
- Session 8: ✅ Issue detail screen & status updates

### 🚧 Coming Soon (Sessions 9-27)
- Worker management and assignment
- User authentication
- Status filtering
- Search functionality
- Real-time sync
- iOS camera integration
- Export reports as PDF
- Push notifications
- Offline mode improvements

## 📱 Screenshots

### Android
- **Issue List**: Clean cards with optional photo thumbnails
- **Create Issue**: Camera integration with live preview
- **Photo Viewer**: Full-screen photo viewing

### iOS
- UI working, camera integration pending

## 🛠 Tech Stack

### Shared Code (~75%)
- **Kotlin Multiplatform**: Business logic shared across platforms
- **Jetpack Compose Multiplatform**: Declarative UI framework
- **SQLDelight 2.0**: Type-safe SQL database
- **Kotlinx Coroutines**: Async/concurrent programming
- **Kotlinx Serialization**: JSON serialization
- **Kotlinx DateTime**: Cross-platform date/time handling
- **StateFlow**: Reactive state management

### Android-Specific (~15%)
- **CameraX**: Modern camera API
- **Coil**: Image loading and caching
- **Accompanist Permissions**: Runtime permission handling

### iOS-Specific (~10%)
- **Native SQLite**: iOS database driver
- **UIKit Integration**: SwiftUI interop (pending)

## 🏗 Architecture
```
ConstructionIssueTracker/
├── composeApp/
│   ├── commonMain/          # 75% - Shared code
│   │   ├── kotlin/
│   │   │   ├── models/      # Data classes (Issue, User)
│   │   │   ├── database/    # expect classes
│   │   │   ├── repository/  # Data access layer
│   │   │   ├── viewmodel/   # Business logic & state
│   │   │   ├── ui/          # Compose UI screens
│   │   │   └── platform/    # expect declarations
│   │   └── sqldelight/      # SQL schema & queries
│   ├── androidMain/         # 15% - Android specific
│   │   └── kotlin/
│   │       ├── database/    # Android SQLite driver
│   │       ├── platform/    # actual implementations
│   │       ├── camera/      # CameraX integration
│   │       └── ui/          # Android image loading
│   └── iosMain/             # 10% - iOS specific
│       └── kotlin/
│           ├── database/    # iOS SQLite driver
│           ├── platform/    # actual implementations
│           └── ui/          # iOS image loading (stub)
└── iosApp/                  # iOS app wrapper
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) with KMP plugin
- **Xcode** 16+ (for iOS, macOS only)
- **JDK** 17
- **macOS** (required for iOS development)

### Run Android
```bash
./gradlew :composeApp:installDebug
```
Or click **Run** ▶️ in Android Studio

### Run iOS
```bash
cd iosApp
open iosApp.xcodeproj
```
Then click **Run** ▶️ in Xcode (Cmd+R)

## 📚 Learning Journey

### Session 1: Environment Setup ✅
- Installed Android Studio, Xcode, JDK
- Created KMP project with Compose Multiplatform
- Set up GitHub repository
- Got "Hello World" running on both platforms

### Session 2: Data Layer ✅
- Learned Kotlin syntax (coming from Swift)
- Created `Issue` and `User` data models
- Designed SQLDelight schema
- Wrote type-safe SQL queries

### Session 3: Architecture & Patterns ✅
- **expect/actual pattern**: Platform abstraction
- Database drivers for Android & iOS
- Repository pattern for data access
- Dependency injection basics

### Session 4: UI & State Management ✅
- Jetpack Compose fundamentals
- `StateFlow` for reactive state
- `ViewModel` lifecycle management
- Built issue list screen (Android + iOS)

### Session 5: Navigation & Forms ✅
- Multi-screen navigation
- Create issue screen with form validation
- ImagePicker interface (expect/actual)
- Navigation state management

### Session 6: Camera Integration ✅
- CameraX implementation (Android)
- Runtime permission handling
- Photo capture and storage
- Fixed navigation flow (camera as overlay)

### Session 7: Image Display ✅
- Coil image loading library
- Photo thumbnails in list
- Full-screen photo viewer
- Optimized layouts (with/without photos)
- expect/actual for image loading

### Next Up
- **Session 8**: Issue detail screen with status updates
- **Session 9**: Worker assignment
- **Session 10**: Filtering and search

## 🎓 Key KMP Concepts Learned

### expect/actual Pattern
```kotlin
// Common code
expect class DatabaseDriver {
    fun create(): SqlDriver
}

// Android implementation
actual class DatabaseDriver(context: Context) {
    actual fun create() = AndroidSqliteDriver(...)
}

// iOS implementation  
actual class DatabaseDriver {
    actual fun create() = NativeSqliteDriver(...)
}
```

### State Management
```kotlin
class IssueListViewModel(repository: IssueRepository) : ViewModel() {
    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()
    
    fun loadIssues() {
        viewModelScope.launch {
            _issues.value = repository.getAllIssues()
        }
    }
}
```

### Compose UI
```kotlin
@Composable
fun IssueCard(issue: Issue) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(issue.flatNumber)
            Text(issue.description)
        }
    }
}
```

## 📊 Project Stats

- **Total Sessions Completed**: 7 / 27 (26%)
- **Time Invested**: ~6-7 hours
- **Lines of Code**: ~1,200
- **Code Sharing**: ~75%
- **Platforms**: Android ✅, iOS ⏳

## 🐛 Known Issues

- iOS camera integration pending
- iOS framework configuration needs refinement
- Photo file validation could be improved

## 📝 License

Personal learning project - feel free to use as reference!

## 👤 Author

**Marek Hajdučák**  
Learning KMP development  
GitHub: [@hajducak](https://github.com/hajducak)

## 🙏 Resources Used

- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [Coil Image Loading](https://coil-kt.github.io/coil/)