# Construction Issue Tracker

A Kotlin Multiplatform (KMP) mobile app for managing construction issues across apartment flats. Built with Jetpack Compose Multiplatform for iOS and Android with ~75% code sharing.

## 🎯 Features

### ✅ Implemented (Sessions 1-10)

#### Core Issue Management
- **Issue List** with photo thumbnails and status indicators
- **Create Issues** with camera integration (Android) / simulation (iOS)
- **Issue Details** with full information display
- **Status Management** with confirmation dialogs (OPEN → IN_PROGRESS → FIXED → VERIFIED)
- **Photo Capture** using CameraX (Android)
- **Photo Display** with thumbnails in list and full-screen viewer

#### Worker Management (Session 9)
- **Worker List** displaying all workers with roles
- **Add Workers** with name input and role selector (MANAGER/WORKER)
- **Assign Workers** to issues via dialog selection
- **Unassign Workers** functionality
- **Worker Filtering** by role
- **Default Worker Seeding** (Mike Johnson, Sarah Williams)

#### Search & Filtering (Session 10)
- **Text Search** by issue description and flat number (case-insensitive)
- **Status Filtering** with toggle chips (4 status options)
- **Worker Filtering** to view issues by assigned worker
- **Combined Filters** working together with AND logic
- **Active Filter Count** badge display
- **Clear All Filters** button
- **Filter Persistence** across navigation and tab switches
- **Empty States** with contextual messages

#### Authentication & User Roles (Session 11)
- **User Login System** with profile selection
- **Session Management** with UserSession (expect/actual pattern)
- **Role-Based Permissions** (Manager vs Worker)
- **Manager Capabilities**: Create issues, assign workers, manage all issues, change any status
- **Worker Capabilities**: View assigned issues only, update status (OPEN→IN_PROGRESS→FIXED)
- **Worker Assignment During Creation** (optional dropdown)
- **Logout Functionality** with session clearing
- **Current User Display** in top app bar
- **Status Flow Restrictions**: Workers can only progress forward (OPEN→IN_PROGRESS→FIXED), managers have full control
- **Filtered Issue Lists**: Workers see only their assigned issues, managers see all
- **Session Persistence**: Login state saved across app restarts

#### Navigation & UX
- **Bottom Navigation Bar** (iOS-style) with Issues and Workers tabs
- **Material Design 3** UI throughout
- **Selected/Unselected States** for tabs and filter chips
- **Confirmation Dialogs** for status changes
- **Toast Notifications** for user feedback
- **Loading States** with progress indicators
- **Form Validation** for inputs

#### Data & Architecture
- **SQLite Database** with SQLDelight (type-safe queries)
- **Repository Pattern** for data access
- **ViewModels** with StateFlow for reactive state
- **Coroutines** for async operations
- **Database Relationships** (Issues ↔ Workers)
- **expect/actual Pattern** for platform-specific code
- **UUID Generation** for unique IDs

### 🚧 Coming Soon (Sessions 11-27)

#### Authentication & Security
- User login/logout
- Role-based permissions (Manager vs Worker)
- Session management
- Secure user storage

#### Advanced Features
- Issue comments/notes
- Issue history/timeline
- File attachments (multiple photos, PDFs)
- Push notifications
- Offline sync
- Export reports (PDF)
- Dashboard with statistics
- Due dates and reminders

#### Polish & Performance
- Image optimization
- Caching strategies
- Error handling improvements
- Accessibility features
- Localization (multiple languages)
- Dark mode

---

## 📱 Screenshots

### Android
- **Issue List**: Clean cards with photo thumbnails and status
- **Search & Filters**: Horizontal scrolling filter chips with active count
- **Create Issue**: Camera integration with live preview
- **Issue Detail**: Full information with worker assignment
- **Workers List**: Worker cards with roles and emoji icons
- **Bottom Navigation**: iOS-style tab bar

### iOS
- UI identical to Android (75% shared code)
- Camera simulation pending real implementation
- All features functional on simulator

---

## 🛠 Tech Stack

### Shared Code (~75%)
- **Kotlin Multiplatform 2.1.0**: Business logic shared across platforms
- **Jetpack Compose Multiplatform 1.7.1**: Declarative UI framework
- **SQLDelight 2.0.2**: Type-safe SQL database with compile-time verification
- **Kotlinx Coroutines 1.9.0**: Async/concurrent programming
- **Kotlinx Serialization 1.7.3**: JSON serialization
- **Kotlinx DateTime 0.6.1**: Cross-platform date/time handling
- **StateFlow**: Reactive state management
- **UUID 0.8.2**: Cross-platform unique ID generation

### Android-Specific (~15%)
- **CameraX 1.3.1**: Modern camera API
- **Coil 2.5.0**: Image loading and caching
- **Accompanist Permissions 0.32.0**: Runtime permission handling
- **AndroidX Lifecycle**: ViewModel and lifecycle management

### iOS-Specific (~10%)
- **Native SQLite Driver**: iOS database implementation
- **UIKit Integration**: SwiftUI interop (camera pending)

---

## 🏗 Architecture
```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  - Compose UI screens (Issues, Workers, Detail, etc.)   │
│  - ViewModels with StateFlow                            │
│  - Navigation logic                                      │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                    Business Logic                        │
│  - Repository Pattern (single source of truth)          │
│  - Data validation                                       │
│  - Filtering & search logic                             │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────┐
│                     Data Layer                           │
│  - SQLDelight Database                                   │
│  - expect/actual DatabaseDriver                         │
│  - Type-safe queries                                     │
└─────────────────────────────────────────────────────────┘
```

### Project Structure
```
ConstructionIssueTracker/
├── composeApp/
│   ├── commonMain/              # 75% - Shared code
│   │   ├── kotlin/
│   │   │   ├── models/          # Data classes (Issue, User)
│   │   │   ├── database/        # expect declarations
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── viewmodel/       # Business logic & state
│   │   │   ├── ui/              # Compose screens & components
│   │   │   ├── platform/        # expect declarations (ImagePicker)
│   │   │   └── App.kt           # Main app with navigation
│   │   ├── sqldelight/          # SQL schema & queries
│   │   └── resources/           # Shared assets
│   ├── androidMain/             # 15% - Android specific
│   │   └── kotlin/
│   │       ├── database/        # AndroidSqliteDriver
│   │       ├── platform/        # ImagePicker with CameraX
│   │       ├── camera/          # CameraCapture composable
│   │       ├── ui/              # Android image loading (Coil)
│   │       └── MainActivity.kt  # Entry point
│   ├── iosMain/                 # 10% - iOS specific
│   │   └── kotlin/
│   │       ├── database/        # NativeSqliteDriver
│   │       ├── platform/        # ImagePicker (simulated)
│   │       ├── ui/              # iOS image loading (placeholder)
│   │       └── MainViewController.kt
│   └── commonTest/              # Unit tests
│       └── kotlin/
│           └── models/          # Model tests (3 passing)
└── iosApp/                      # iOS app wrapper (Swift)
    └── iosApp/
        ├── ContentView.swift    # SwiftUI integration
        └── Info.plist
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) with Kotlin Multiplatform plugin
- **Xcode** 16+ (for iOS, macOS only)
- **JDK** 17 (at `/usr/local/opt/openjdk@17/`)
- **macOS** (required for iOS development)

### Clone Repository
```bash
git clone https://github.com/hajducak/construction-issue-tracker.git
cd construction-issue-tracker
```

### Run Android
```bash
./gradlew :composeApp:installDebug
```
Or click **Run** ▶️ in Android Studio (select `composeApp` configuration)

### Run iOS
```bash
cd iosApp
open iosApp.xcodeproj
```
Then click **Run** ▶️ in Xcode (Cmd+R) with iPhone simulator selected

---

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew :composeApp:testDebugUnitTest

# Run with verbose output
./gradlew :composeApp:testDebugUnitTest --info
```

### Current Test Coverage
- ✅ **Models**: Data class creation, copy functionality, enum validation (3 tests)
- 🚧 **Repository**: Coming soon with in-memory SQLite
- 🚧 **ViewModels**: Coming soon with fake repositories

### Writing Tests
Tests are located in `composeApp/src/commonTest/kotlin/`

Example:
```kotlin
import kotlin.test.*

class IssueTest {
    @Test
    fun `issue creation with required fields`() {
        // Given
        val issue = Issue(...)
        
        // Then
        assertEquals("expected", issue.property)
    }
}
```

---

## 📚 Learning Journey

### Session 1: Environment Setup ✅
**What:** Project setup and configuration  
**Learned:** Android Studio, Xcode setup, KMP project structure, running on both platforms

### Session 2: Data Layer ✅
**What:** Data models and database schema  
**Learned:** Kotlin syntax (val/var, data classes), SQLDelight basics, SQL queries

### Session 3: Architecture Patterns ✅
**What:** expect/actual pattern and repository  
**Learned:** Platform abstraction, database drivers (Android/iOS), repository pattern, ~70% code sharing achieved

### Session 4: UI & State Management ✅
**What:** Issue list screen with real data  
**Learned:** Jetpack Compose basics, StateFlow for reactive state, ViewModel lifecycle, LazyColumn, Material 3 components

### Session 5: Multi-Screen Navigation ✅
**What:** Create issue screen and navigation  
**Learned:** Navigation state management, form handling, validation, screen composition

### Session 6: Camera Integration ✅
**What:** Real photo capture (Android)  
**Learned:** CameraX implementation, permission handling, photo storage, lifecycle management, camera as overlay

### Session 7: Image Display ✅
**What:** Photo thumbnails and viewer  
**Learned:** Coil library for image loading, full-screen dialog, expect/actual for images, optimized layouts

### Session 8: Issue Details & Status Updates ✅
**What:** Detail screen with status management  
**Learned:** Dropdown menus, confirmation dialogs, toast notifications, status workflow, user feedback patterns

### Session 9: Worker Management ✅
**What:** Worker CRUD and assignment system  
**Learned:**
- Bottom navigation (iOS-style)
- Database relationships (foreign keys conceptually)
- Worker-to-issue assignment
- UUID generation for unique IDs
- Role-based data (MANAGER vs WORKER)
- Dialog-based assignment UI
- Tab-based navigation patterns

### Session 10: Filtering & Search ✅
**What:** Advanced filtering system  
**Learned:**
- Text search with real-time updates
- Multi-criteria filtering (status + worker + search)
- Filter chip UI with horizontal scrolling
- Combined filters with AND logic
- ViewModel state persistence across navigation
- StateFlow combination with `stateIn`
- Active filter count with derived state
- Clear all functionality
- Empty state handling

### Session 11: Authentication & User Roles ✅
**What:** Login system with role-based permissions  
**Learned:**
- expect/actual for platform-specific storage (SharedPreferences/UserDefaults)
- Serialization with kotlinx.serialization
- Session management patterns
- Role-based UI rendering
- Permission systems and access control
- Status workflow restrictions
- Conditional navigation (hide tabs based on role)
- User context propagation through ViewModels

---

## 🎓 Key KMP Concepts Mastered

### 1. expect/actual Pattern (Platform Abstraction)
```kotlin
// Common code - what you need
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// Android - how Android does it
actual class DatabaseDriverFactory(context: Context) {
    actual fun createDriver() = AndroidSqliteDriver(...)
}

// iOS - how iOS does it
actual class DatabaseDriverFactory {
    actual fun createDriver() = NativeSqliteDriver(...)
}
```

**Use cases in our app:**
- Database drivers (SQLite for each platform)
- Image picker (CameraX vs UIImagePickerController)
- Image loading (Coil vs native iOS)

### 2. State Management with StateFlow
```kotlin
class IssueListViewModel(repository: IssueRepository) : ViewModel() {
    // Private mutable state (only ViewModel changes this)
    private val _issues = MutableStateFlow<List<Issue>>(emptyList())
    
    // Public read-only state (UI observes this)
    val issues: StateFlow<List<Issue>> = _issues.asStateFlow()
    
    // Derived state (combines multiple flows)
    val activeFilterCount: StateFlow<Int> = combine(
        _selectedStatus, _selectedWorker, _searchQuery
    ) { status, worker, search ->
        // Calculate count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    fun loadIssues() {
        viewModelScope.launch {
            _issues.value = repository.getAllIssues()
        }
    }
}
```

**Benefits:**
- Reactive UI updates automatically
- Type-safe state management
- Lifecycle-aware (no memory leaks)
- Easy to test

### 3. Repository Pattern
```kotlin
class IssueRepository(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = FixItDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.fixItDatabaseQueries
    
    suspend fun getAllIssues(): List<Issue> = 
        dbQuery.selectAllIssues().executeAsList().map { /* convert */ }
    
    suspend fun insertIssue(issue: Issue) = 
        dbQuery.insertIssue(...)
}
```

**Benefits:**
- Single source of truth
- Easy to swap data sources (local ↔ remote)
- Testable with fake implementations
- Hides implementation details

### 4. Jetpack Compose (Declarative UI)
```kotlin
@Composable
fun IssueCard(issue: Issue) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(issue.flatNumber)
            Text(issue.description)
            StatusChip(issue.status)
        }
    }
}
```

**Benefits:**
- UI as a function of state
- No XML layouts
- Automatic recomposition on state change
- Compose works on Android AND iOS

---

## 📊 Project Statistics

**Development Time:** ~12 hours  
**Sessions Completed:** 10 / 27 (37%)  
**Code Written:** ~2,500 lines  
**Code Sharing:** ~75%  
**Screens:** 5 (Issues List, Create Issue, Issue Detail, Workers List, Add Worker)  
**ViewModels:** 6  
**Database Tables:** 2 (Issue, User)  
**Tests:** 3 passing ✅

### Platform Status
| Feature | Android | iOS |
|---------|---------|-----|
| Issue List | ✅ | ✅ |
| Create Issue | ✅ | ✅ |
| Camera | ✅ Real | ⏳ Simulated |
| Photo Display | ✅ Coil | ✅ Placeholder |
| Issue Detail | ✅ | ✅ |
| Status Updates | ✅ | ✅ |
| Workers List | ✅ | ✅ |
| Add Worker | ✅ | ✅ |
| Worker Assignment | ✅ | ✅ |
| Search & Filters | ✅ | ✅ |
| Bottom Nav | ✅ | ✅ |
| Database | ✅ SQLite | ✅ SQLite |

---

## 🎯 Roadmap

### Phase 1: Core Features (✅ Complete)
- [x] Issue CRUD operations
- [x] Photo capture and display
- [x] Worker management
- [x] Worker assignment
- [x] Search and filtering
- [x] Bottom navigation

### Phase 2: Authentication (In Progress)
- [ ] User login system
- [ ] Role-based permissions
- [ ] Session management
- [ ] Logout functionality

### Phase 3: Advanced Features
- [ ] Issue comments
- [ ] Multiple photos per issue
- [ ] Issue history timeline
- [ ] Push notifications
- [ ] Dashboard with charts

### Phase 4: Polish
- [ ] Real iOS camera
- [ ] Dark mode
- [ ] Localization
- [ ] Error handling
- [ ] Accessibility
- [ ] Performance optimization

---

## 💡 What Makes This App Special

### For Learning:
- **Real-world architecture** used in production apps
- **Professional patterns** (MVVM, Repository, etc.)
- **Cross-platform development** with 75% code reuse
- **Type-safe everything** (database, state, navigation)
- **Modern Android/iOS best practices**

### For Production:
- **Scalable architecture** ready for more features
- **Offline-first** with local database
- **Clean separation** of concerns
- **Testable code** with dependency injection
- **Material Design 3** for modern UI
- **Real filtering system** with multiple criteria

---

## 🐛 Known Issues

- iOS camera uses simulated paths (real UIImagePickerController pending)
- Test coverage needs expansion (currently ~10%)
- No network layer yet (offline only)
- No user authentication (coming in Session 11)

---

## 🤝 Contributing

This is a learning project documenting the journey of building a KMP app from scratch. Each session builds on the previous one with clear explanations of concepts.

---

## 📝 License

Personal learning project - MIT License

---

## 👤 Author

**Marek Hajdučák**  
Learning Kotlin Multiplatform Mobile Development  
Repository: [github.com/hajducak/construction-issue-tracker](https://github.com/hajducak/construction-issue-tracker)

---

## 🙏 Acknowledgments

- JetBrains for Kotlin Multiplatform and Compose Multiplatform
- Google for Android development tools
- Apple for iOS development tools
- Anthropic Claude for guidance and code assistance
- SQLDelight team for amazing database tooling
- Open source community for libraries used

---

## 📖 Additional Resources

- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)