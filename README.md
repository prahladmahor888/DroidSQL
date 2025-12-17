# PocketSQL - Offline Database Terminal for Android

![Build Status](https://img.shields.io/badge/build-ready-brightgreen) ![API](https://img.shields.io/badge/API-24%2B-blue) ![License](https://img.shields.io/badge/license-Academic-yellow)

## 🚀 Project Overview

**PocketSQL** is a professional offline Android application that functions as a universal SQLite database management terminal. Execute raw SQL commands, visualize dynamic query results, and manage databases directly on your Android device—no internet required.

Built using **MVVM architecture**, this project demonstrates deep understanding of:
- Database systems and SQL execution
- Dynamic UI rendering
- Android development best practices
- Algorithm complexity optimization

---

## ✨ Key Features

### Core Functionality
- ✅ **Raw SQL Execution** - Direct SQLite access without ORM abstraction
- ✅ **Universal Database Client** - Works with any table schema
- ✅ **Dynamic Result Rendering** - Tables adapt to any column count
- ✅ **Full CRUD Support** - CREATE, READ, UPDATE, DELETE operations

### Advanced Features
- 🕒 **Command History** - Previous/Next navigation (O(1) complexity)
- 📝 **SQL Templates** - Pre-built examples for learning
- 💾 **Database Export** - Save to Downloads folder
- 🎨 **Terminal UI** - Professional green-on-black interface
- 🔍 **Error Handling** - Graceful error messages
- 📊 **Performance Tips** - O(log N) optimization guidance

---

## 🏗️ Architecture

**MVVM Pattern** (Model-View-ViewModel)

```
┌─────────────────────────────────────────┐
│          View Layer                     │
│  MainActivity + activity_main.xml       │
│  (Terminal UI, Tables, Buttons)         │
└──────────────┬──────────────────────────┘
               │ LiveData
               ↓
┌──────────────────────────────────────────┐
│       ViewModel Layer                    │
│   DatabaseViewModel                      │
│   (State Management, LiveData)           │
└──────────────┬───────────────────────────┘
               │
               ↓
┌──────────────────────────────────────────┐
│        Model Layer                       │
│  DatabaseManager + QueryResult           │
│  (Raw SQLite Operations)                 │
└──────────────────────────────────────────┘
```

---

## 🔧 Technical Stack

- **Language**: Java 11
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Architecture**: MVVM
- **Database**: Android SQLite (native)
- **UI**: ViewBinding + LiveData
- **Build System**: Gradle (Kotlin DSL)

---

## 📦 Project Structure

```
app/src/main/java/com/smartqueue/droidsql/
├── model/
│   ├── DatabaseManager.java       # SQLite operations
│   ├── QueryResult.java           # Data container
│   └── SQLCommand.java            # History record
├── viewmodel/
│   └── DatabaseViewModel.java     # State management
├── utils/
│   └── SQLTemplateHelper.java     # SQL templates
└── MainActivity.java               # UI controller

app/src/main/res/
├── layout/
│   └── activity_main.xml          # Terminal interface
└── values/
    └── colors.xml                 # Terminal theme
```

---

## 🚦 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- JDK 11 or higher
- Android device/emulator with API 24+

### Build & Run

#### Option 1: Android Studio
1. Open project in Android Studio
2. Sync Gradle: `File → Sync Project with Gradle Files`
3. Run on device/emulator: `Run → Run 'app'`

#### Option 2: Command Line
```bash
cd c:\Users\asus\AndroidStudioProjects\DroidSQL

# Build APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and install
./gradlew build installDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📖 Usage Guide

### 1. Open/Create Database
```
1. Enter database name (e.g., "mydb.db")
2. Click "OPEN" button
3. Status shows: "● Database: mydb.db"
```

### 2. Execute SQL Commands

**Create Table:**
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT UNIQUE,
  age INTEGER
);
```

**Insert Data:**
```sql
INSERT INTO users (name, email, age)
VALUES ('John Doe', 'john@example.com', 25);
```

**Query Data:**
```sql
SELECT * FROM users;
```

**Update Data:**
```sql
UPDATE users SET age = 26 WHERE name = 'John Doe';
```

**Delete Data:**
```sql
DELETE FROM users WHERE age < 18;
```

### 3. Use Command History
- Click **◄ PREV** to load previous command
- Click **NEXT ►** to navigate forward
- History navigation is instant (O(1))

### 4. SQL Templates
- Click **TEMPLATES** button
- Select from 10 pre-built examples
- Click **Performance Tips** for O(log N) optimization advice

### 5. Export Database
- Click **EXPORT** button
- Database saved to `Downloads/` folder
- Open on PC with DB Browser for SQLite

---

## ⚡ Performance Characteristics

| Component | Complexity | Notes |
|-----------|------------|-------|
| SQLite SELECT (indexed) | O(log N) | B-tree internal structure |
| SQLite SELECT (no index) | O(N) | Table scan required |
| Command history navigation | O(1) | ArrayList index access |
| Dynamic table rendering | O(N*M) | N rows × M columns (unavoidable) |
| Terminal log append | O(1) amortized | StringBuilder |
| Database export | O(F) | F = file size |

**Optimization Tips:**
- Create indexes on frequently queried columns
- Use `PRIMARY KEY` for automatic indexing
- Use `CREATE INDEX` for O(log N) lookups
- Analyze queries with `EXPLAIN QUERY PLAN`

---

## 🎓 Academic Value

This project demonstrates:

### 1. Database Internals
- Raw SQL execution (not hiding behind Room ORM)
- Understanding of B-tree indexing in SQLite
- Query optimization and performance analysis

### 2. Algorithm Complexity
- O(log N) for indexed database lookups
- O(1) for command history navigation
- O(N*M) recognition for unavoidable scenarios

### 3. Software Engineering
- Clean MVVM architecture
- Reactive programming with LiveData
- ViewBinding for type safety
- Comprehensive error handling

### 4. Android Development
- Lifecycle-aware components
- Dynamic UI construction at runtime
- File system operations
- Permission handling

### 5. Professional Practices
- Extensive code documentation
- Complexity analysis annotations
- User-friendly error messages
- Offline-first design

---

## 📝 Testing Checklist

Manual testing procedures:

- [ ] Open database successfully
- [ ] Execute CREATE TABLE
- [ ] Execute INSERT commands
- [ ] Execute SELECT with dynamic columns
- [ ] Execute UPDATE and verify changes
- [ ] Execute DELETE and verify removal
- [ ] Navigate command history (Prev/Next)
- [ ] Load SQL templates
- [ ] Export database to Downloads
- [ ] Test error handling (invalid SQL)
- [ ] Test terminal clear function
- [ ] Test list tables function
- [ ] Verify color-coded terminal output

See `walkthrough.md` for detailed testing procedures.

---

## 🎯 Presentation Keywords

For academic evaluation:

- **Universal Database Client** - Schema-agnostic design
- **Dynamic UI Rendering** - Runtime table generation
- **Raw SQL Execution** - Direct SQLite API usage
- **Offline-First Architecture** - No network dependency
- **O(log N) Optimization** - B-tree indexing awareness
- **MVVM Design Pattern** - Clean architecture
- **Reactive Programming** - LiveData observers

---

## 📄 License

Academic Project - For Educational Purposes

---

## 👨‍💻 Project Stats

- **Total Files**: 10
- **Lines of Code**: ~1,200 (Java)
- **Classes Created**: 6
- **XML Resources**: 3
- **Architecture**: MVVM
- **Test Coverage**: Manual testing procedures documented

---

## 🔗 Documentation

- [Implementation Plan](file:///C:/Users/asus/.gemini/antigravity/brain/826f8ab8-36ac-4a6a-8e51-2b462eed9826/implementation_plan.md) - Technical design document
- [Walkthrough](file:///C:/Users/asus/.gemini/antigravity/brain/826f8ab8-36ac-4a6a-8e51-2b462eed9826/walkthrough.md) - Complete testing guide and performance analysis
- [Task Breakdown](file:///C:/Users/asus/.gemini/antigravity/brain/826f8ab8-36ac-4a6a-8e51-2b462eed9826/task.md) - Implementation progress tracking

---

## 🚀 Quick Start Example

```sql
-- Create database
Open: shop.db

-- Create table
CREATE TABLE products (
  id INTEGER PRIMARY KEY,
  name TEXT,
  price REAL
);

-- Insert data
INSERT INTO products VALUES (1, 'Laptop', 999.99);
INSERT INTO products VALUES (2, 'Mouse', 19.99);

-- Query (dynamic table renders automatically)
SELECT * FROM products WHERE price > 50;

-- Result: Shows Laptop only, with 3 columns (id, name, price)
```

---

**Made with ❤️ for Academic Excellence**
