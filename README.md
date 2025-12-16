# FixIt - Construction Issue Tracker

A modern, cross-platform mobile application for tracking and managing construction site issues built with Kotlin Multiplatform and Compose Multiplatform.

## 🎯 Project Overview

FixIt is a comprehensive issue tracking system designed for construction site management. It enables managers to document issues with photos, assign work to contractors, and track progress through a complete workflow from identification to verification.

**Platform Support:** Android & iOS

## ✅ Implemented Features

### Core Issue Management (Sessions 1-8)
- **Issue Creation** with flat number, description, and photo capture
- **Issue List View** with status indicators and filtering
- **Issue Detail Screen** with full information display
- **Photo Capture** using CameraX (Android) and UIImagePickerController (iOS)
- **SQLDelight Database** with Issue and User tables
- **Repository Pattern** for data access with error handling
- **CRUD Operations** for issues (Create, Read, Update, Delete)
- **Multi-Screen Navigation** with state management
- **Status Workflow**: OPEN → IN_PROGRESS → FIXED → VERIFIED

### Worker Management (Session 9)
- **Worker CRUD Operations** (Create, Read, Update, Delete)
- **Worker List Screen** with add/edit functionality
- **Issue Assignment** to workers
- **Worker Selection** during issue creation
- **UUID Generation** for all entities
- **Bottom Navigation** (iOS-style tab bar)
- **Navigation Tabs**: Dashboard, Issues, Workers

### Filtering & Search (Session 10)
- **Text Search** across issue descriptions and flat numbers
- **Status Filter** dropdown (All, Open, In Progress, Fixed, Verified)
- **Worker Filter** dropdown for filtering by assigned worker
- **Combined AND Logic** for multiple filters
- **Filter State Persistence** across navigation
- **Active Filter Count Badge** showing number of active filters
- **Clear All Filters** button
- **Real-Time Filtering** as user types
- **Empty State Messages** for filtered results

### Authentication & Authorization (Session 11)
- **Login System** with username/password
- **User Session Management** with expect/actual pattern
- **SharedPreferences** (Android) and UserDefaults (iOS) for persistence
- **Role-Based Access Control**: Manager and Worker roles
- **Logout Functionality** with session clearing
- **Session Persistence** across app restarts
- **Login Screen** with error handling
- **Auto-Login** if session exists
- **Status Change Restrictions** based on role:
    - Workers: Can move OPEN → IN_PROGRESS → FIXED
    - Managers: Full control over all status changes
    - Workers cannot set VERIFIED status
- **Permission-Based UI** hiding unavailable options

### Validation & Error Handling (Session 12)
- **Flat Number Validation** with regex pattern (A-101 format)
- **Description Length Validation** (10-500 characters)
- **Inline Error Messages** on form fields
- **Auto-Uppercase** for flat numbers
- **Validation Utility Class** with reusable validators
- **Snackbar Notifications** for success/error messages
- **Repository Error Wrapping** with descriptive messages
- **Form Validation** preventing invalid submissions
- **Real-Time Error Clearing** as user corrects input
- **Character Counter** on description field

### Comments System (Session 13)
- **Comment Model** with userId, text, timestamp
- **Comment Database Table** with foreign keys
- **CommentWithUser** wrapper joining comments with user data
- **Add Comments** with real-time posting
- **Delete Comments** with confirmation dialog
- **Role-Based Delete Permissions**:
    - Users can delete own comments
    - Managers can delete any comment
- **Comment Display** with user name, role badge, timestamp
- **Own Comment Highlighting** with different background color
- **Comments Section** in issue detail screen
- **Comment Counter** showing total comments
- **Empty State** for issues with no comments
- **Loading States** for async operations
- **Snackbar Feedback** for comment actions

### Activity Timeline & History (Session 14)
- **ActivityLog Model** with type, old/new values, timestamp
- **ActivityLog Database Table** with foreign keys
- **Activity Types**: Created, Status Changed, Assigned, Unassigned, Comment Added, Comment Deleted, Photo Added, Photo Deleted
- **Automatic Logging** for all issue changes
- **Timeline UI** in issue detail screen (below comments)
- **Activity Icons**: ✨ Created, 🔄 Status, 👷 Assigned, ❌ Unassigned, 💬 Comment, 📷 Photo Added, 🖼️ Photo Deleted
- **Activity Descriptions** with old/new value display
- **Reverse Chronological Order** (newest first)
- **User Attribution** for all activities
- **Real-Time Timeline Updates** after actions
- **Complete Audit Trail** for compliance
- **ActivityLogWithUser** joins for user details
- **Loading States** and empty states

### Multiple Photos per Issue (Session 15)
- **Photo Model** with issueId, photoPath, uploadedBy, createdAt
- **Photo Database Table** with foreign keys to Issue and User
- **Multiple Photo Upload** during issue creation
- **Photo Gallery** on issue detail screen with thumbnails
- **Add Photos** after issue creation (managers only)
- **Delete Photos** individually with confirmation (managers only)
- **Photo Counter** showing total photos (e.g., "Photos (3)")
- **Full-Screen Photo Viewer** on thumbnail click
- **Photo Activity Logging**: Added/deleted tracked in timeline
- **Timeline Icons**: 📷 for added, 🖼️ for deleted photos
- **Photo Management UI** with PhotoGallerySection and PhotoGalleryItem
- **Worker Permissions**: View-only access to photos
- **Real-Time Updates** after adding/deleting photos
- **Photo Metadata** displayed (number, timestamp, uploader)
- **Empty State** for issues without photos
- **Removed photoPath from Issue model** (breaking change)

### Dashboard & Statistics (Session 16)
- **Manager Dashboard** with system-wide overview
- **Worker Dashboard** with personal statistics
- **Overview Section**: Total issues, completion rate, workers, overdue count
- **Status Breakdown**: Visual progress bars for each status
- **Worker Performance**: Completion rates for all workers (manager only)
- **Personal Stats**: Assigned and completed issues (worker view)
- **Completion Rate Calculation**: Percentage of verified issues
- **Role-Based Views**: Different dashboards for managers vs workers
- **Dashboard Navigation**: New bottom tab, default landing screen
- **Statistics Repository Methods**: getDashboardStatistics, getWorkerPersonalStatistics
- **Real-Time Updates**: Statistics refresh on navigation
- **Visual Progress Bars**: Color-coded by status
- **StatCard Components**: Reusable metric cards with icons
- **Empty State Handling**: Messages for workers with no assignments
- **Pull-to-Refresh**: Manual statistics refresh

### Due Dates & Priority (Session 17)
- **Priority Levels**: LOW 🟢, MEDIUM 🟡, HIGH 🟠, URGENT 🔴
- **Priority Selector** dropdown in create issue screen
- **Priority Badges** on issue list with color-coded containers
- **Due Date Picker** with Material Design date picker dialog
- **Optional Due Dates** for flexible deadline management
- **Overdue Detection** with automatic calculation
- **Overdue Warnings**: ⚠️ indicator and red highlighting
- **Days Until Due** countdown with smart messages ("Due today", "Due in 3 days", "Overdue by 2 days")
- **Overdue Dashboard Card** showing count of overdue issues
- **Worker Overdue Tracking** in personal dashboard
- **Priority Display** on issue detail with icons
- **Due Date Display** with detailed countdown information
- **Date Formatting** with timezone-aware display
- **Color-Coded Priorities**: Different container colors per priority level
- **Clear Due Date** button for removing deadlines
- **Overdue Exclusion**: Verified issues not counted as overdue

### Export & Reporting (Session 18)
- **PDF Export System** with platform-specific implementations
- **Single Issue Export**: Generate detailed PDF report for any issue
- **Bulk Export**: Export all issues to comprehensive PDF report
- **Android PDF Generation**: Native PdfDocument API with custom canvas drawing
- **iOS PDF Generation**: Native UIGraphics PDF context with CoreGraphics
- **Professional Formatting**: Tables, colors, proper typography
- **Priority Icons**: Emoji-based priority display in PDFs
- **Overdue Warnings**: Red highlighting for overdue issues in exports
- **Text Wrapping**: Automatic line breaking for long descriptions
- **File Management**: Save to Downloads (Android) / Documents (iOS)
- **Unique Filenames**: Timestamp-based naming prevents overwrites
- **Export UI**: Buttons in issue detail and issue list screens
- **Loading States**: Spinners and disabled states during export
- **Success Feedback**: Snackbar notifications with file location
- **Error Handling**: Graceful failure with user-friendly messages

## 🏗️ Architecture & Technology Stack

### Core Technologies
- **Kotlin Multiplatform** - Shared business logic
- **Compose Multiplatform** - UI framework for Android & iOS
- **SQLDelight** - Type-safe SQL database
- **Coroutines & Flow** - Asynchronous programming
- **ViewModel Pattern** - State management
- **Repository Pattern** - Data layer abstraction
- **kotlinx.datetime** - Cross-platform date/time handling

### Platform-Specific
- **Android**: CameraX for photo capture, SharedPreferences for session, PdfDocument for exports
- **iOS**: UIImagePickerController for photos, UserDefaults for session, UIGraphics for exports

### Database Schema
- **Tables**: Issue (with priority & dueDate), User, Comment, ActivityLog, Photo
- **Foreign Keys**: Enforcing referential integrity
- **Queries**: Type-safe SQL with SQLDelight

## 📁 Project Structure

```
composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/com/hajducakmarek/fixit/
│   │   │   ├── models/          # Data models
│   │   │   │   ├── Issue.kt
│   │   │   │   ├── User.kt
│   │   │   │   ├── Comment.kt
│   │   │   │   ├── ActivityLog.kt
│   │   │   │   ├── Photo.kt
│   │   │   │   └── Statistics.kt
│   │   │   ├── repository/      # Data layer
│   │   │   │   └── IssueRepository.kt
│   │   │   ├── viewmodel/       # ViewModels
│   │   │   │   ├── IssueListViewModel.kt
│   │   │   │   ├── IssueDetailViewModel.kt
│   │   │   │   ├── CreateIssueViewModel.kt
│   │   │   │   ├── WorkerListViewModel.kt
│   │   │   │   ├── AddWorkerViewModel.kt
│   │   │   │   ├── LoginViewModel.kt
│   │   │   │   └── DashboardViewModel.kt
│   │   │   ├── ui/              # UI screens
│   │   │   │   ├── IssueListScreen.kt
│   │   │   │   ├── IssueDetailScreen.kt
│   │   │   │   ├── CreateIssueScreen.kt
│   │   │   │   ├── WorkerListScreen.kt
│   │   │   │   ├── AddWorkerScreen.kt
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── DashboardScreen.kt
│   │   │   │   └── CommonUI.kt
│   │   │   ├── utils/           # Utilities
│   │   │   │   ├── Validation.kt
│   │   │   │   └── PdfExporter.kt
│   │   │   ├── session/         # Session management
│   │   │   │   └── UserSession.kt (expect)
│   │   │   ├── platform/        # Platform interfaces
│   │   │   │   └── ImagePicker.kt (expect)
│   │   │   ├── database/        # Database
│   │   │   │   └── DatabaseDriverFactory.kt (expect)
│   │   │   └── App.kt           # Main app
│   │   └── sqldelight/
│   │       └── com/hajducakmarek/fixit/database/
│   │           └── FixItDatabase.sq
│   ├── androidMain/             # Android-specific
│   │   └── kotlin/com/hajducakmarek/fixit/
│   │       ├── MainActivity.kt
│   │       ├── database/DatabaseDriverFactory.kt
│   │       ├── session/UserSession.kt
│   │       ├── platform/ImagePicker.kt
│   │       ├── utils/PdfExporter.android.kt
│   │       └── ui/CameraOverlay.kt
│   └── iosMain/                 # iOS-specific
│       └── kotlin/com/hajducakmarek/fixit/
│           ├── MainViewController.kt
│           ├── database/DatabaseDriverFactory.kt
│           ├── session/UserSession.kt
│           ├── platform/ImagePicker.kt
│           ├── utils/PdfExporter.ios.kt
│           └── ui/CameraOverlay.kt
```

## 📊 Database Schema

### Issue Table
```sql
CREATE TABLE Issue (
    id TEXT PRIMARY KEY NOT NULL,
    description TEXT NOT NULL,
    flatNumber TEXT NOT NULL,
    status TEXT NOT NULL,
    createdBy TEXT NOT NULL,
    assignedTo TEXT,
    createdAt INTEGER NOT NULL,
    completedAt INTEGER,
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    dueDate INTEGER,
    FOREIGN KEY (createdBy) REFERENCES User(id),
    FOREIGN KEY (assignedTo) REFERENCES User(id)
)
```

### User Table
```sql
CREATE TABLE User (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL
)
```

### Comment Table
```sql
CREATE TABLE Comment (
    id TEXT PRIMARY KEY NOT NULL,
    issueId TEXT NOT NULL,
    userId TEXT NOT NULL,
    text TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (issueId) REFERENCES Issue(id),
    FOREIGN KEY (userId) REFERENCES User(id)
)
```

### ActivityLog Table
```sql
CREATE TABLE ActivityLog (
    id TEXT PRIMARY KEY NOT NULL,
    issueId TEXT NOT NULL,
    userId TEXT NOT NULL,
    activityType TEXT NOT NULL,
    oldValue TEXT,
    newValue TEXT,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (issueId) REFERENCES Issue(id),
    FOREIGN KEY (userId) REFERENCES User(id)
)
```

### Photo Table
```sql
CREATE TABLE Photo (
    id TEXT PRIMARY KEY NOT NULL,
    issueId TEXT NOT NULL,
    photoPath TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    uploadedBy TEXT NOT NULL,
    FOREIGN KEY (issueId) REFERENCES Issue(id),
    FOREIGN KEY (uploadedBy) REFERENCES User(id)
)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Xcode (for iOS development)
- JDK 17 or higher
- Kotlin 2.0+

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/ConstructionIssueTracker.git
cd ConstructionIssueTracker
```

2. **Android Setup**
```bash
./gradlew :composeApp:installDebug
```

3. **iOS Setup**
```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

### Default Users (Seeded in Database)

**Manager Account:**
- Username: `manager`
- Password: `password`
- Role: MANAGER

**Worker Accounts:**
- Username: `worker1` (Mike Johnson)
- Password: `password`
- Role: WORKER

- Username: `worker2` (Sarah Williams)
- Password: `password`
- Role: WORKER

## 📚 Learning Journey

### Session 1-8: Core Features ✅
**What:** Basic CRUD, database, navigation, photo capture  
**Learned:**
- Kotlin Multiplatform project structure
- Compose Multiplatform UI framework
- SQLDelight database setup and queries
- Repository pattern implementation
- ViewModel state management with StateFlow
- Multi-screen navigation in Compose
- Platform-specific photo capture (CameraX, UIImagePickerController)
- expect/actual mechanism for platform code
- Image storage and retrieval

### Session 9: Worker Management ✅
**What:** Worker CRUD, assignment system, bottom navigation  
**Learned:**
- Bottom navigation implementation
- Tab-based navigation pattern
- Worker management UI
- Issue assignment workflow
- UUID generation for entities
- Relationship management in database

### Session 10: Filtering & Search ✅
**What:** Text search, status/worker filters, combined filtering  
**Learned:**
- Real-time search implementation
- Dropdown filter components
- Combined filter logic (AND operations)
- State management for filters
- Filter persistence across navigation
- Empty state handling
- Badge components for active filters

### Session 11: Authentication & Authorization ✅
**What:** Login system, session management, role-based permissions  
**Learned:**
- expect/actual pattern for platform storage
- SharedPreferences (Android) and UserDefaults (iOS)
- Session persistence across app restarts
- Role-based access control (RBAC)
- Permission-based UI rendering
- Login screen design
- Auto-login on app start
- Secure logout with session clearing

### Session 12: Validation & Error Handling ✅
**What:** Form validation, inline errors, user feedback  
**Learned:**
- Regex validation patterns
- Reusable validation utility classes
- Inline error message display
- Auto-uppercase text transformation
- Character counting
- Snackbar notifications
- Repository error wrapping
- Form state validation

### Session 13: Comments System ✅
**What:** Comments with CRUD, role-based permissions  
**Learned:**
- One-to-many database relationships
- JOIN queries with SQLDelight
- Real-time comment posting
- Confirmation dialogs for destructive actions
- Role-based delete permissions
- Comment UI with user attribution
- Own comment highlighting
- Empty state patterns

### Session 14: Activity Timeline & History ✅
**What:** Complete audit trail for all issue changes  
**Learned:**
- Audit logging patterns
- Automatic activity tracking
- Timeline UI design
- Activity type enumeration
- Old/new value tracking
- Chronological data display
- Activity icons and descriptions
- Real-time timeline updates

### Session 15: Multiple Photos per Issue ✅
**What:** Photo gallery system with upload/delete  
**Learned:**
- One-to-many database relationships
- Breaking schema changes (removed photoPath from Issue)
- List-based state management in ViewModels
- Photo gallery UI patterns
- Thumbnail + full-screen viewer pattern
- Real-time list updates
- Activity logging for media operations
- Role-based photo permissions
- Confirmation dialogs for destructive actions
- Photo metadata tracking

### Session 16: Dashboard & Statistics ✅
**What:** Role-based dashboard with visual statistics  
**Learned:**
- Aggregating data from multiple sources
- Complex statistics calculation
- Role-based UI rendering
- Progress bar visualization
- Percentage calculations
- State management for statistics
- Dashboard design patterns
- Visual data representation
- Personal vs system-wide metrics
- Color-coded progress indicators
- Card-based layout composition
- Default landing screen configuration

### Session 17: Due Dates & Priority ✅
**What:** Priority levels and due date management  
**Learned:**
- Enum-based priority system
- Optional nullable fields in models
- Material Design date picker integration
- Date/time handling with kotlinx.datetime
- Timezone-aware date calculations
- Days until due countdown logic
- Overdue detection algorithms
- Color-coded UI based on priority/status
- Pull-to-refresh implementation
- Auto-refresh with LaunchedEffect
- Conditional color in composables
- Smart date display messages
- Filtering overdue vs completed issues
- Dashboard metric calculations
- Breaking schema changes with defaults

### Session 18: Export & Reporting ✅
**What:** PDF generation with platform-specific implementations  
**Learned:**
- expect/actual pattern for platform-specific APIs
- Android PdfDocument API usage
- iOS UIGraphics PDF context
- Canvas/CoreGraphics drawing
- Custom text rendering and wrapping
- Paint/NSFont typography systems
- File system access (Downloads/Documents)
- Platform-specific file paths
- Unique filename generation with timestamps
- Text measurement and wrapping algorithms
- Color application in PDFs
- Table layout in generated documents
- Triple return types for complex data
- Export state management in ViewModels
- Button loading states with spinners
- Snackbar success notifications
- Platform differences in PDF generation
- Native API advantages vs third-party libraries

## 📈 Project Statistics

- **Sessions Completed:** 18 / 27 (67%)
- **Code Written:** ~6,100 lines
- **Development Time:** ~24 hours
- **Screens:** 7 (Login, Dashboard, Issues, Create, Detail, Workers, Add Worker)
- **ViewModels:** 7
- **Database Tables:** 5 (Issue with priority/dueDate, User, Comment, ActivityLog, Photo)
- **Platform Support:** Android ✅, iOS ✅

## 🎯 Next Features (Sessions 19-27)

### Session 19: Advanced Filtering & Search
- Priority filter dropdown
- Due date range filter
- Overdue issues filter
- Multiple filter combinations
- Filter chips UI
- Save filter presets

### Session 20: Notifications System
- Push notifications for assignments
- Status change notifications
- Comment notifications
- Due date reminders

### Session 21: Offline Support
- Offline-first architecture
- Sync mechanism
- Conflict resolution
- Queue management

### Session 22: Settings & Preferences
- User preferences
- App settings
- Notification settings
- Theme customization

### Session 23: Barcode Scanning
- QR code scanning for flat numbers
- Barcode generation
- Quick issue lookup

### Session 24: Voice Notes
- Audio recording
- Voice note attachments
- Playback controls

### Session 25: Map Integration
- Location tracking
- Issue map view
- GPS coordinates

### Session 26: Testing & Quality
- Unit tests
- Integration tests
- UI tests
- Test coverage

### Session 27: Deployment & Polish
- App store preparation
- Release builds
- Performance optimization
- Final polish

## 🤝 Contributing

This is a learning project following a structured tutorial series. Each session builds upon the previous one, teaching Kotlin Multiplatform concepts progressively.

## 📝 License

This project is created for educational purposes.

## 🙏 Acknowledgments

Built as part of a comprehensive Kotlin Multiplatform learning journey, covering real-world mobile app development patterns and best practices.