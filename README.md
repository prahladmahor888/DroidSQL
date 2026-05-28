# DroidSQL - Offline Database Terminal for Android

![Build Status](https://img.shields.io/badge/build-ready-brightgreen) ![API](https://img.shields.io/badge/API-24%2B-blue) ![License](https://img.shields.io/badge/license-Academic-yellow)

## 🚀 Project Overview

**DroidSQL** is a professional offline Android application that functions as a universal SQLite database management terminal. Execute raw SQL commands, visualize dynamic query results, and manage databases directly on your Android device—no internet required.

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

## 🔒 Security & Encryption

DroidSQL implements a state-of-the-art secure local offline storage architecture:

### 1. Database Location (Internal Private Storage)
By default, all SQLite database files (`.db`) are stored in the app's sandboxed internal private storage directory:
- `/data/data/com.smartqueue.droidsql/databases/`

This location is protected by Android's sandbox security system, ensuring that other applications cannot read or write to DroidSQL's database files directly.

### 2. SQLCipher Database Encryption (AES-256)
Instead of standard unencrypted SQLite databases, DroidSQL uses **SQLCipher** to automatically encrypt all database files at rest with military-grade **AES-256** encryption.
- Any attempt to open or copy database files directly (e.g., via root file browsers or memory dumps) will show only encrypted/unreadable binary data.
- Plain-text databases are automatically migrated to encrypted SQLCipher format on-the-fly upon opening.

### 3. Android Keystore Key Protection
To prevent hardcoded password reverse-engineering attacks:
- The database encryption password is generated dynamically using `SecureRandom` on first startup.
- The password is encrypted using a unique symmetric AES-GCM key generated in the hardware-backed **Android Keystore System** (`AndroidKeyStore` provider).
- The encrypted key material is stored in private `SharedPreferences`, and decrypted securely in memory at runtime when launching database sessions.

### 4. Screenshot & Video Protection
- **FLAG_SECURE** is enforced across sensitive SQL terminal views, completely blocking screenshot captures and video screen recordings.
- Disables preview exposure in Android's recent app switcher to prevent unauthorized background shoulder-surfing.

### 5. ADB Backup Vectors Disabled
- Configuration has `android:allowBackup="false"` in the manifest to prevent unauthorized raw data extraction via ADB backup and restore operations.

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

---

## 🌐 REST API Server (Local Network Access)

DroidSQL includes a built-in **local HTTP REST API server** that lets you query your Android databases from any device on the same Wi-Fi network — without needing cloud services or USB cables.

### ⚡ How to Start the Server

1. Open DroidSQL on your Android device
2. Tap the **⚙️ Settings** button in the toolbar
3. Tap **"🌐 SQL REST API: STOPPED"**
4. The server starts on port **8080** and a details dialog appears showing:
   - Your device's local IP address (e.g., `192.168.1.5`)
   - A randomly generated security token
   - Ready-to-use curl command examples

### 🔐 Authentication

Every request **must** include the security token, either as:
- **HTTP Header**: `X-API-Key: <YOUR_TOKEN>`
- **Query Parameter**: `?token=<YOUR_TOKEN>`

A fresh token is generated every time you start the server. You can copy it from the **"Copy Token"** button shown after starting the server.

---

### 📱➡️💻 Connecting from Mobile to PC — Step by Step

> **Goal:** Access the DroidSQL REST API running on your Android phone from your PC or Laptop.

#### ✅ Prerequisites (Check These First)

| Condition | How to Verify |
|-----------|--------------|
| Phone and PC are on the same Wi-Fi network | Both should show the same router/network name |
| DroidSQL app is installed and open | Check your phone screen |
| Mobile Data is turned OFF on Android | Mobile data can conflict with Wi-Fi IP routing |
| Firewall allows port 8080 on PC | See the Windows Firewall fix section below |

---

#### 🔵 STEP 1 — Start the Server on Your Android Phone

1. Open the DroidSQL app
2. Tap the **⚙️ Settings** button in the toolbar
3. Tap **"🌐 SQL REST API: STOPPED"**
4. The server will start — a dialog appears showing:
   ```
   Status:    RUNNING
   IP Addr:   192.168.1.XXX       ← note this IP / copy it
   Port:      8080
   Token:     AbCd1234            ← copy this token
   ```
5. Press **"Copy Token"** — the token is copied to your clipboard

---

#### 🔵 STEP 2 — Confirm the Android IP Address

If the IP shown in the app seems incorrect, check it manually in your phone's Wi-Fi settings:

```
Android Phone →
  Settings → Wi-Fi → Tap on Connected Network →
  Look for "IP Address" → e.g. 192.168.1.5
```

---

#### 🔵 STEP 3 — Test the Connection from PC

Open a **browser** on your PC and navigate to:

```
http://192.168.1.5:8080/status?token=AbCd1234
```

> Replace `192.168.1.5` with your phone's actual IP address  
> Replace `AbCd1234` with the token copied from the app

**Expected response:**
```json
{
  "status": "running",
  "database": "ecommerce.db",
  "is_database_open": true
}
```

---

#### 🔵 STEP 4 — Query from PC using cURL

> ⚠️ **Important (Windows Users):** In PowerShell, `curl` is an alias for `Invoke-WebRequest`, NOT the real curl command.  
> Use one of the three options below:

---

**Option A — Use `curl.exe` in PowerShell (recommended — Windows 10/11 built-in)**

```powershell
# Status check
curl.exe -H "X-API-Key: svY762HN" http://192.168.1.10:8080/status

# SELECT query (GET)
curl.exe -H "X-API-Key: svY762HN" "http://192.168.1.10:8080/query?sql=SELECT+*+FROM+sqlite_master;"

# POST — query with JSON body
curl.exe -X POST -H "Content-Type: application/json" -H "X-API-Key: svY762HN" -d "{\"sql\": \"SELECT * FROM ecommerce.products;\"}" http://192.168.1.10:8080/query
```

> 💡 Appending `.exe` forces PowerShell to use the real curl binary instead of its alias

---

**Option B — Use `Invoke-WebRequest` (native PowerShell syntax)**

```powershell
# Status check
Invoke-WebRequest -Uri "http://192.168.1.10:8080/status" `
  -Headers @{ "X-API-Key" = "svY762HN" } | Select-Object -ExpandProperty Content

# SELECT query (GET)
Invoke-WebRequest -Uri "http://192.168.1.10:8080/query?sql=SELECT+*+FROM+sqlite_master;" `
  -Headers @{ "X-API-Key" = "svY762HN" } | Select-Object -ExpandProperty Content

# POST — query with JSON body
Invoke-WebRequest -Uri "http://192.168.1.10:8080/query" `
  -Method POST `
  -Headers @{ "X-API-Key" = "svY762HN" } `
  -ContentType "application/json" `
  -Body '{"sql": "SELECT * FROM ecommerce.products;"}' | Select-Object -ExpandProperty Content
```

---

**Option C — Use Command Prompt (cmd.exe) — `curl` works directly here**

```cmd
REM Status check
curl -H "X-API-Key: svY762HN" http://192.168.1.10:8080/status

REM SELECT query
curl -H "X-API-Key: svY762HN" "http://192.168.1.10:8080/query?sql=SELECT+*+FROM+sqlite_master;"

REM POST query
curl -X POST -H "Content-Type: application/json" -H "X-API-Key: svY762HN" -d "{\"sql\": \"SELECT * FROM sqlite_master;\"}" http://192.168.1.10:8080/query
```

---

**Linux / macOS Terminal:**
```bash
# Status check
curl -H "X-API-Key: svY762HN" http://192.168.1.10:8080/status

# SELECT query
curl -H "X-API-Key: svY762HN" \
     "http://192.168.1.10:8080/query?sql=SELECT+*+FROM+sqlite_master;"

# POST query
curl -X POST \
     -H "Content-Type: application/json" \
     -H "X-API-Key: svY762HN" \
     -d '{"sql": "SELECT * FROM sqlite_master;"}' \
     http://192.168.1.10:8080/query
```

---

#### 🔵 STEP 5 — Test using Postman (GUI Alternative)

If you prefer a graphical interface instead of the command line, use **Postman** (free tool):

1. Download Postman: https://www.postman.com/downloads/
2. Click **New Request → GET**
3. Enter the URL: `http://192.168.1.5:8080/status`
4. Go to the **Headers** tab and add:
   - Key: `X-API-Key`
   - Value: `AbCd1234`
5. Click **Send** → JSON response will appear

---

#### 🔧 Troubleshooting — Connection Not Working?

| Error | Solution |
|-------|----------|
| `Connection refused` | Start the server from DroidSQL Settings |
| `No route to host` | Are both devices on the same Wi-Fi? Turn off mobile data |
| `401 Unauthorized` | Wrong token — copy it again from the app |
| `404 Not Found` | Check the URL path — should be `/status` or `/query` |
| Browser can't open URL | Allow port 8080 in Windows Firewall (see below) |
| IP shows `127.0.0.1` | Phone not connected to Wi-Fi — enable Wi-Fi on Android |

---

#### 🛡️ Windows Firewall Fix (If Connection is Blocked)

Open **PowerShell as Administrator** and run:

```powershell
# Allow inbound connections on port 8080
New-NetFirewallRule -DisplayName "DroidSQL API" `
  -Direction Inbound `
  -Protocol TCP `
  -LocalPort 8080 `
  -Action Allow
```

Or configure manually:
```
Windows Defender Firewall →
  Advanced Settings →
  Inbound Rules → New Rule →
  Port → TCP → 8080 →
  Allow the connection → Finish
```

---

#### ✅ Connection Success Checklist

```
[ ] Phone and PC are on the same Wi-Fi network
[ ] DroidSQL shows "🌐 SQL REST API: RUNNING"
[ ] IP address noted/copied from the app dialog
[ ] Token copied using the "Copy Token" button
[ ] Browser opened: http://<IP>:8080/status?token=<TOKEN>
[ ] JSON response received: {"status": "running", ...}
```

---



### 📡 API Endpoints

#### `GET /status`
Returns the current server status and active database name.

**Response:**
```json
{
  "status": "running",
  "database": "ecommerce.db",
  "is_database_open": true
}
```

---

#### `GET /query?sql=<SQL_STATEMENT>&token=<TOKEN>`
#### `POST /query` (with JSON body `{"sql": "<SQL_STATEMENT>"}`)

Executes any SQL statement and returns the result as JSON.

**Response (SELECT):**
```json
{
  "success": true,
  "execution_time_ms": 3,
  "message": "Query OK",
  "rows_affected": 2,
  "columns": ["id", "name", "email"],
  "rows": [
    ["1", "Alice", "alice@example.com"],
    ["2", "Bob", "bob@example.com"]
  ]
}
```

**Response (INSERT / UPDATE / DELETE):**
```json
{
  "success": true,
  "execution_time_ms": 1,
  "message": "Query OK, 1 row affected",
  "rows_affected": 1
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "ERROR: no such table: users"
}
```

---

### 💻 Code Examples in Multiple Languages

Replace `192.168.1.5` with your Android device's IP address and `<YOUR_TOKEN>` with the token shown in the app.

---

#### 🖥️ cURL (Terminal/Command Line)

```bash
# Check server status
curl -H "X-API-Key: <YOUR_TOKEN>" \
     "http://192.168.1.5:8080/status"

# GET request — Simple SELECT query
curl -H "X-API-Key: <YOUR_TOKEN>" \
     "http://192.168.1.5:8080/query?sql=SELECT+*+FROM+users;"

# POST request — JSON body (recommended for complex queries)
curl -X POST \
     -H "Content-Type: application/json" \
     -H "X-API-Key: <YOUR_TOKEN>" \
     -d '{"sql": "SELECT id, name FROM users WHERE age > 20;"}' \
     "http://192.168.1.5:8080/query"

# INSERT data using POST
curl -X POST \
     -H "Content-Type: application/json" \
     -H "X-API-Key: <YOUR_TOKEN>" \
     -d '{"sql": "INSERT INTO users (name, email, age) VALUES (\"Alice\", \"alice@example.com\", 25);"}' \
     "http://192.168.1.5:8080/query"
```

---

#### 🐍 Python

```python
import requests
import json

BASE_URL = "http://192.168.1.5:8080"
TOKEN = "<YOUR_TOKEN>"
HEADERS = {"X-API-Key": TOKEN}

# Check status
status = requests.get(f"{BASE_URL}/status", headers=HEADERS)
print(status.json())

# SELECT query — GET method
response = requests.get(
    f"{BASE_URL}/query",
    params={"sql": "SELECT * FROM users;"},
    headers=HEADERS
)
data = response.json()

if data["success"] and "rows" in data:
    columns = data["columns"]
    for row in data["rows"]:
        record = dict(zip(columns, row))
        print(record)

# INSERT record — POST method
payload = {"sql": "INSERT INTO users (name, email, age) VALUES ('Bob', 'bob@example.com', 30);"}
response = requests.post(f"{BASE_URL}/query", json=payload, headers=HEADERS)
print(response.json())

# CREATE TABLE
create_sql = """
CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price REAL
);
"""
response = requests.post(f"{BASE_URL}/query", json={"sql": create_sql}, headers=HEADERS)
print(response.json())
```

---

#### 🌐 JavaScript (Browser / Node.js)

```javascript
const BASE_URL = "http://192.168.1.5:8080";
const TOKEN = "<YOUR_TOKEN>";

// Check server status
async function getStatus() {
    const res = await fetch(`${BASE_URL}/status`, {
        headers: { "X-API-Key": TOKEN }
    });
    const data = await res.json();
    console.log("Status:", data);
}

// Execute a SELECT query
async function runQuery(sql) {
    const res = await fetch(`${BASE_URL}/query`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-API-Key": TOKEN
        },
        body: JSON.stringify({ sql })
    });
    const data = await res.json();
    if (data.success && data.columns) {
        // Map rows to objects with column names as keys
        const rows = data.rows.map(row =>
            Object.fromEntries(data.columns.map((col, i) => [col, row[i]]))
        );
        console.log(rows);
    } else {
        console.error("Error:", data.error);
    }
}

// Insert data
async function insertUser(name, email, age) {
    const sql = `INSERT INTO users (name, email, age) VALUES ('${name}', '${email}', ${age});`;
    const res = await fetch(`${BASE_URL}/query`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-API-Key": TOKEN
        },
        body: JSON.stringify({ sql })
    });
    const data = await res.json();
    console.log("Inserted:", data.rows_affected, "row(s)");
}

// Usage
getStatus();
runQuery("SELECT * FROM users;");
insertUser("Charlie", "charlie@example.com", 28);
```

---

#### ☕ Java (Standard / Android)

```java
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import org.json.*;

public class DroidSQLClient {
    private static final String BASE_URL = "http://192.168.1.5:8080";
    private static final String TOKEN = "<YOUR_TOKEN>";

    public static JSONObject getStatus() throws Exception {
        URL url = new URL(BASE_URL + "/status");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-API-Key", TOKEN);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return new JSONObject(sb.toString());
    }

    public static JSONObject runQuery(String sql) throws Exception {
        URL url = new URL(BASE_URL + "/query");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-API-Key", TOKEN);
        conn.setDoOutput(true);

        String body = new JSONObject().put("sql", sql).toString();
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return new JSONObject(sb.toString());
    }

    public static void main(String[] args) throws Exception {
        // Get status
        System.out.println(getStatus());

        // Select query
        JSONObject result = runQuery("SELECT * FROM users;");
        if (result.getBoolean("success") && result.has("rows")) {
            JSONArray columns = result.getJSONArray("columns");
            JSONArray rows = result.getJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                JSONArray row = rows.getJSONArray(i);
                for (int j = 0; j < columns.length(); j++) {
                    System.out.print(columns.getString(j) + ": " + row.getString(j) + "  ");
                }
                System.out.println();
            }
        }
    }
}
```

---

#### 🐘 PHP

> **Full Client File:** [`examples/droidsql_client.php`](file:///c:/Users/ASUS/AndroidStudioProjects/DroidSQL/examples/droidsql_client.php)  
> Run करो: `php examples/droidsql_client.php`

```php
<?php

// ============================================================
//  CONFIGURATION
// ============================================================
$BASE_URL = "http://192.168.1.5:8080";   // Android device IP
$TOKEN    = "<YOUR_TOKEN>";               // App se copy karo

// ============================================================
//  DROIDSQL CLIENT CLASS
// ============================================================
class DroidSQLClient {
    private string $baseUrl;
    private string $token;

    public function __construct(string $baseUrl, string $token) {
        $this->baseUrl = rtrim($baseUrl, '/');
        $this->token   = $token;
    }

    // Server status
    public function getStatus(): array {
        return $this->get('/status');
    }

    // SQL POST se bhejo (recommended)
    public function query(string $sql): array {
        return $this->post('/query', ['sql' => $sql]);
    }

    // SELECT → associative array return karta hai
    public function select(string $sql): array {
        $result = $this->query($sql);
        if (!$result['success'] || !isset($result['rows'])) return [];
        $columns = $result['columns'];
        return array_map(fn($row) => array_combine($columns, $row), $result['rows']);
    }

    // INSERT / UPDATE / DELETE → affected rows return karta hai
    public function execute(string $sql): int {
        $result = $this->query($sql);
        if (!$result['success']) {
            echo "[ERROR] " . ($result['error'] ?? 'Unknown') . "\n";
            return -1;
        }
        return $result['rows_affected'] ?? 0;
    }

    // Table create karo
    public function createTable(string $sql): bool {
        return ($this->query($sql))['success'] ?? false;
    }

    // --- Internal GET ---
    private function get(string $path): array {
        $sep = str_contains($path, '?') ? '&' : '?';
        $url = $this->baseUrl . $path . $sep . 'token=' . urlencode($this->token);
        $ch  = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT        => 10,
            CURLOPT_HTTPHEADER     => ["X-API-Key: {$this->token}"],
        ]);
        $res = curl_exec($ch); curl_close($ch);
        return json_decode($res, true) ?? ['success' => false, 'error' => 'Invalid JSON'];
    }

    // --- Internal POST ---
    private function post(string $path, array $body): array {
        $json = json_encode($body);
        $ch   = curl_init($this->baseUrl . $path);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST           => true,
            CURLOPT_TIMEOUT        => 15,
            CURLOPT_POSTFIELDS     => $json,
            CURLOPT_HTTPHEADER     => [
                "Content-Type: application/json",
                "X-API-Key: {$this->token}",
                "Content-Length: " . strlen($json)
            ],
        ]);
        $res = curl_exec($ch); curl_close($ch);
        return json_decode($res, true) ?? ['success' => false, 'error' => 'Invalid JSON'];
    }
}

// ============================================================
//  EXAMPLE — Full CRUD demo
// ============================================================
$db = new DroidSQLClient($BASE_URL, $TOKEN);

// 1. Status check
$status = $db->getStatus();
echo "✅ Connected to: " . $status['database'] . "\n\n";

// 2. Table banao
$db->createTable("
    CREATE TABLE IF NOT EXISTS students (
        id   INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        age  INTEGER,
        city TEXT
    );
");

// 3. Data insert karo
$db->execute("INSERT INTO students (name, age, city) VALUES ('Rahul', 20, 'Delhi');");
$db->execute("INSERT INTO students (name, age, city) VALUES ('Priya', 22, 'Mumbai');");
$db->execute("INSERT INTO students (name, age, city) VALUES ('Amit',  19, 'Pune');");

// 4. SELECT — sabhi records
echo "--- All Students ---\n";
$students = $db->select("SELECT * FROM students;");
foreach ($students as $s) {
    echo "  [{$s['id']}] {$s['name']} | Age: {$s['age']} | City: {$s['city']}\n";
}

// 5. WHERE filter
echo "\n--- Age > 20 ---\n";
$filtered = $db->select("SELECT name, city FROM students WHERE age > 20;");
foreach ($filtered as $s) {
    echo "  👤 {$s['name']} — {$s['city']}\n";
}

// 6. UPDATE
$db->execute("UPDATE students SET city = 'Kolkata' WHERE name = 'Rahul';");
echo "\n✅ Rahul ki city update ho gayi!\n";

// 7. DELETE
$deleted = $db->execute("DELETE FROM students WHERE name = 'Amit';");
echo "✅ $deleted row(s) delete ho gayi\n";

// 8. COUNT
$result = $db->query("SELECT COUNT(*) as total FROM students;");
echo "📊 Total students ab: " . $result['rows'][0][0] . "\n";
?>
```

**Kaise Run Karein:**
```bash
# Terminal mein yeh command chalao
php examples/droidsql_client.php
```

**Prerequisites:**
- PHP 7.4+ installed hona chahiye
- `php_curl` extension enabled honi chahiye (`php.ini` mein check karo)
- DroidSQL app mein REST API server **RUNNING** hona chahiye
- PHP script aur Android device **ek hi Wi-Fi** par hone chahiye

---


#### 🔷 C# (.NET / Unity)

```csharp
using System;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;   // or System.Text.Json

class DroidSQLClient
{
    private static readonly string BASE_URL = "http://192.168.1.5:8080";
    private static readonly string TOKEN = "<YOUR_TOKEN>";
    private static readonly HttpClient client = new HttpClient();

    static async Task<JObject> GetStatus()
    {
        client.DefaultRequestHeaders.Remove("X-API-Key");
        client.DefaultRequestHeaders.Add("X-API-Key", TOKEN);
        var response = await client.GetStringAsync($"{BASE_URL}/status");
        return JObject.Parse(response);
    }

    static async Task<JObject> RunQuery(string sql)
    {
        var payload = new JObject { ["sql"] = sql }.ToString();
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        client.DefaultRequestHeaders.Remove("X-API-Key");
        client.DefaultRequestHeaders.Add("X-API-Key", TOKEN);

        var response = await client.PostAsync($"{BASE_URL}/query", content);
        var body = await response.Content.ReadAsStringAsync();
        return JObject.Parse(body);
    }

    static async Task Main()
    {
        // Check status
        var status = await GetStatus();
        Console.WriteLine($"Database: {status["database"]}");

        // Run SELECT
        var result = await RunQuery("SELECT * FROM users;");
        if ((bool)result["success"] && result["columns"] != null)
        {
            var columns = result["columns"].ToObject<string[]>();
            var rows = result["rows"] as JArray;
            foreach (var row in rows)
            {
                var rowArr = row.ToObject<string[]>();
                for (int i = 0; i < columns.Length; i++)
                    Console.Write($"{columns[i]}: {rowArr[i]}  ");
                Console.WriteLine();
            }
        }

        // Insert record
        var insert = await RunQuery(
            "INSERT INTO users (name, email, age) VALUES ('Eve', 'eve@example.com', 22);"
        );
        Console.WriteLine($"Rows inserted: {insert["rows_affected"]}");
    }
}
```

---

#### 🦀 Dart / Flutter

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

const String BASE_URL = "http://192.168.1.5:8080";
const String TOKEN = "<YOUR_TOKEN>";

Map<String, String> get headers => {
  "X-API-Key": TOKEN,
  "Content-Type": "application/json",
};

// Check server status
Future<Map> getStatus() async {
  final res = await http.get(Uri.parse("$BASE_URL/status"), headers: {"X-API-Key": TOKEN});
  return jsonDecode(res.body);
}

// Run any SQL query
Future<Map> runQuery(String sql) async {
  final res = await http.post(
    Uri.parse("$BASE_URL/query"),
    headers: headers,
    body: jsonEncode({"sql": sql}),
  );
  return jsonDecode(res.body);
}

void main() async {
  // Status
  final status = await getStatus();
  print("Connected to: ${status['database']}");

  // SELECT
  final result = await runQuery("SELECT * FROM users;");
  if (result['success'] == true && result.containsKey('rows')) {
    final columns = List<String>.from(result['columns']);
    final rows = List<List>.from(result['rows']);
    for (var row in rows) {
      final record = Map.fromIterables(columns, row);
      print(record);
    }
  }

  // INSERT
  final insert = await runQuery(
    "INSERT INTO users (name, email, age) VALUES ('Frank', 'frank@example.com', 29);"
  );
  print("Rows affected: ${insert['rows_affected']}");
}
```

---

### ⚠️ Security Notes

| Rule | Detail |
|------|--------|
| **Local Network Only** | The server binds to the device's LAN IP — not accessible from the internet |
| **Token Required** | All requests need the `X-API-Key` header or `token` query parameter |
| **New Token on Each Start** | The token regenerates each time you start the server |
| **Stop When Done** | Always stop the server when you are not using it to reduce attack surface |
| **Same Wi-Fi Only** | Connecting device and Android device must be on the same Wi-Fi network |

---
