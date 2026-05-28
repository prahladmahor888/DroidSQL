<?php

/**
 * DroidSQL REST API Client — PHP
 * ================================
 * 
 * Ye file DroidSQL Android app ke local REST API server se
 * connect karne ka complete PHP example hai.
 * 
 * Prerequisites:
 *  - PHP 7.4 ya upar
 *  - cURL extension enabled hona chahiye (php_curl)
 *  - DroidSQL app mein REST API server running hona chahiye
 * 
 * Usage:
 *  1. $BASE_URL mein apna Android device ka IP aur port daalo
 *  2. $TOKEN mein DroidSQL app se copy kiya hua token daalo
 *  3. `php droidsql_client.php` se run karo
 */

// ============================================================
//  CONFIGURATION — Apni values yahan daalo
// ============================================================

$BASE_URL = "http://192.168.1.5:8080";   // Android device ka local IP
$TOKEN    = "<YOUR_TOKEN>";               // DroidSQL app se token copy karo

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

    // ----------------------------------------------------------
    // 1. SERVER STATUS CHECK
    // ----------------------------------------------------------

    /**
     * Server ka status check karo.
     * Returns: array ['status', 'database', 'is_database_open']
     */
    public function getStatus(): array {
        return $this->get('/status');
    }

    // ----------------------------------------------------------
    // 2. SQL QUERY EXECUTE (GET method)
    // ----------------------------------------------------------

    /**
     * Simple SELECT query GET request se bhejo.
     * Short queries ke liye use karo.
     * 
     * @param string $sql  SQL statement
     * @return array       JSON response as array
     */
    public function queryGet(string $sql): array {
        $encoded = urlencode($sql);
        return $this->get("/query?sql={$encoded}");
    }

    // ----------------------------------------------------------
    // 3. SQL QUERY EXECUTE (POST method — RECOMMENDED)
    // ----------------------------------------------------------

    /**
     * SQL query POST request se bhejo (JSON body).
     * Complex queries, INSERT, UPDATE, DELETE ke liye use karo.
     * 
     * @param string $sql  SQL statement
     * @return array       JSON response as array
     */
    public function query(string $sql): array {
        return $this->post('/query', ['sql' => $sql]);
    }

    // ----------------------------------------------------------
    // 4. CONVENIENT HELPER METHODS
    // ----------------------------------------------------------

    /**
     * SELECT result ko associative array ki list mein convert karo.
     * Column names key banenge, values value.
     * 
     * @param string $sql  SELECT statement
     * @return array[]     Array of associative arrays (rows)
     */
    public function select(string $sql): array {
        $result = $this->query($sql);

        if (!$result['success'] || !isset($result['rows'])) {
            return [];
        }

        $columns = $result['columns'];
        $rows    = [];

        foreach ($result['rows'] as $row) {
            $rows[] = array_combine($columns, $row);
        }

        return $rows;
    }

    /**
     * INSERT / UPDATE / DELETE execute karo.
     * Returns affected rows count ya -1 on error.
     * 
     * @param string $sql  DML statement
     * @return int         Rows affected
     */
    public function execute(string $sql): int {
        $result = $this->query($sql);
        if (!$result['success']) {
            echo "[ERROR] " . ($result['error'] ?? 'Unknown error') . "\n";
            return -1;
        }
        return $result['rows_affected'] ?? 0;
    }

    /**
     * Table create karo agar exist nahi karta.
     * 
     * @param string $createSQL  CREATE TABLE statement
     * @return bool              Success ya nahi
     */
    public function createTable(string $createSQL): bool {
        $result = $this->query($createSQL);
        return $result['success'] ?? false;
    }

    // ----------------------------------------------------------
    // 5. INTERNAL HTTP HELPERS
    // ----------------------------------------------------------

    private function get(string $path): array {
        $separator = str_contains($path, '?') ? '&' : '?';
        $url = $this->baseUrl . $path . $separator . 'token=' . urlencode($this->token);

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_TIMEOUT        => 10,
            CURLOPT_HTTPHEADER     => [
                "X-API-Key: {$this->token}",
                "Accept: application/json"
            ],
        ]);

        $response = curl_exec($ch);
        $error    = curl_error($ch);
        curl_close($ch);

        if ($error) {
            return ['success' => false, 'error' => "cURL error: $error"];
        }

        return json_decode($response, true) ?? ['success' => false, 'error' => 'Invalid JSON'];
    }

    private function post(string $path, array $body): array {
        $url  = $this->baseUrl . $path;
        $json = json_encode($body);

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST           => true,
            CURLOPT_TIMEOUT        => 15,
            CURLOPT_POSTFIELDS     => $json,
            CURLOPT_HTTPHEADER     => [
                "Content-Type: application/json",
                "X-API-Key: {$this->token}",
                "Accept: application/json",
                "Content-Length: " . strlen($json)
            ],
        ]);

        $response = curl_exec($ch);
        $error    = curl_error($ch);
        curl_close($ch);

        if ($error) {
            return ['success' => false, 'error' => "cURL error: $error"];
        }

        return json_decode($response, true) ?? ['success' => false, 'error' => 'Invalid JSON'];
    }
}

// ============================================================
//  EXAMPLE USAGE — Neeche examples dekho
// ============================================================

$db = new DroidSQLClient($BASE_URL, $TOKEN);

echo "=== DroidSQL PHP Client Example ===\n\n";

// ----------------------------------------------------------
// STEP 1: Server status check karo
// ----------------------------------------------------------
echo "--- [1] Server Status ---\n";
$status = $db->getStatus();

if (isset($status['error'])) {
    echo "❌ Server se connect nahi ho paya: " . $status['error'] . "\n";
    echo "   Ensure karo ki:\n";
    echo "   1. DroidSQL app mein REST API RUNNING hai\n";
    echo "   2. IP aur Token sahi hain\n";
    echo "   3. Dono device ek hi Wi-Fi par hain\n";
    exit(1);
}

echo "✅ Connected!\n";
echo "   Status:   " . $status['status'] . "\n";
echo "   Database: " . $status['database'] . "\n";
echo "   Open:     " . ($status['is_database_open'] ? 'Yes' : 'No') . "\n\n";

// ----------------------------------------------------------
// STEP 2: Table banao (agar nahi hai toh)
// ----------------------------------------------------------
echo "--- [2] Table Create karo ---\n";

$createSQL = "
CREATE TABLE IF NOT EXISTS students (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    age  INTEGER,
    city TEXT
);
";

$created = $db->createTable($createSQL);
echo ($created ? "✅" : "⚠️") . " Table 'students' ready\n\n";

// ----------------------------------------------------------
// STEP 3: Data insert karo
// ----------------------------------------------------------
echo "--- [3] Data Insert karo ---\n";

$inserts = [
    "INSERT INTO students (name, age, city) VALUES ('Rahul Sharma', 20, 'Delhi');",
    "INSERT INTO students (name, age, city) VALUES ('Priya Singh', 22, 'Mumbai');",
    "INSERT INTO students (name, age, city) VALUES ('Amit Verma', 19, 'Pune');",
    "INSERT INTO students (name, age, city) VALUES ('Sneha Gupta', 21, 'Jaipur');",
];

foreach ($inserts as $sql) {
    $affected = $db->execute($sql);
    if ($affected >= 0) {
        echo "✅ Row inserted\n";
    }
}
echo "\n";

// ----------------------------------------------------------
// STEP 4: SELECT — sabhi students dikhao
// ----------------------------------------------------------
echo "--- [4] Sabhi Students ---\n";

$students = $db->select("SELECT * FROM students;");

if (empty($students)) {
    echo "Koi record nahi mila.\n";
} else {
    echo sprintf("%-5s %-20s %-5s %-15s\n", "ID", "Name", "Age", "City");
    echo str_repeat("-", 50) . "\n";
    foreach ($students as $s) {
        echo sprintf("%-5s %-20s %-5s %-15s\n",
            $s['id'], $s['name'], $s['age'], $s['city']
        );
    }
}
echo "\n";

// ----------------------------------------------------------
// STEP 5: WHERE filter se search karo
// ----------------------------------------------------------
echo "--- [5] Age > 20 wale Students ---\n";

$filtered = $db->select("SELECT name, city FROM students WHERE age > 20;");

foreach ($filtered as $s) {
    echo "  👤 " . $s['name'] . " — " . $s['city'] . "\n";
}
echo "\n";

// ----------------------------------------------------------
// STEP 6: UPDATE — ek record update karo
// ----------------------------------------------------------
echo "--- [6] Record Update karo ---\n";

$updated = $db->execute("UPDATE students SET city = 'Kolkata' WHERE name = 'Rahul Sharma';");
echo "✅ Rows updated: $updated\n\n";

// ----------------------------------------------------------
// STEP 7: SELECT — verify update
// ----------------------------------------------------------
echo "--- [7] Updated Record Verify karo ---\n";

$rahul = $db->select("SELECT * FROM students WHERE name = 'Rahul Sharma';");
if (!empty($rahul)) {
    echo "  Name: " . $rahul[0]['name'] . "\n";
    echo "  City: " . $rahul[0]['city'] . " (updated!)\n";
}
echo "\n";

// ----------------------------------------------------------
// STEP 8: Raw query — direct result bhi le sakte ho
// ----------------------------------------------------------
echo "--- [8] Count karo ---\n";

$result = $db->query("SELECT COUNT(*) as total FROM students;");
if ($result['success'] && isset($result['rows'])) {
    echo "  Total students: " . $result['rows'][0][0] . "\n";
}
echo "\n";

// ----------------------------------------------------------
// STEP 9: DELETE — ek record delete karo
// ----------------------------------------------------------
echo "--- [9] Record Delete karo ---\n";

$deleted = $db->execute("DELETE FROM students WHERE name = 'Amit Verma';");
echo "✅ Rows deleted: $deleted\n\n";

// ----------------------------------------------------------
// STEP 10: Final list
// ----------------------------------------------------------
echo "--- [10] Final Students List ---\n";

$final = $db->select("SELECT id, name, age, city FROM students ORDER BY age DESC;");

echo sprintf("%-5s %-20s %-5s %-15s\n", "ID", "Name", "Age", "City");
echo str_repeat("-", 50) . "\n";
foreach ($final as $s) {
    echo sprintf("%-5s %-20s %-5s %-15s\n",
        $s['id'], $s['name'], $s['age'], $s['city']
    );
}

echo "\n✅ PHP Client Example complete!\n";
