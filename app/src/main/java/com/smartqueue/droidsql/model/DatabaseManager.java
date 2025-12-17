package com.smartqueue.droidsql.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages all SQLite database operations with raw SQL execution.
 * 
 * Performance Characteristics:
 * - SQLite internally uses B-trees for indexed columns: O(log N) lookups
 * - Query execution: O(N) for table scans, O(log N) for indexed searches
 * - Our result conversion: O(N*M) where N=rows, M=columns (unavoidable)
 * 
 * Recommendations for O(log N) performance:
 * - Always create indexes on frequently queried columns
 * - Use PRIMARY KEY for unique identifiers
 * - Use WHERE clauses with indexed columns
 */
public class DatabaseManager {
    private SQLiteDatabase database;
    private Context context;
    private String currentDatabaseName;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    /**
     * Opens or creates a database file.
     * Complexity: O(1) - file open operation
     */
    public boolean openOrCreateDatabase(String dbName) {
        try {
            closeDatabase(); // Close any existing database
            database = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
            
            // OPTIMIZATION: Enable Write-Ahead Logging (WAL)
            // Improves write performance and concurrency.
            database.enableWriteAheadLogging();
            
            // OPTIMIZATION: Set synchronous mode to NORMAL
            // Reduces I/O cost significantly while remaining safe for most crashes (except power loss)
            database.execSQL("PRAGMA synchronous = NORMAL;");
            
            // Enable Foreign Key support (disabled by default in SQLite)
            database.execSQL("PRAGMA foreign_keys = ON;");
            
            currentDatabaseName = dbName;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Executes a SQL command with intelligent routing.
     * Routes to rawQuery() for SELECT/PRAGMA or execSQL() for CREATE/INSERT/UPDATE/DELETE.
     * 
     * Special Commands:
     * - CREATE DATABASE dbname - Creates and opens a new database file
     * 
     * Complexity: 
     * - Command analysis: O(1)
     * - Execution: Depends on query (O(N) to O(log N) with indexes)
     * - Result conversion: O(N*M) for N rows and M columns
     */
    public QueryResult executeSQL(String sql) {
        sql = sql.trim();
        if (sql.isEmpty()) {
            return new QueryResult(false, "ERROR: Empty SQL command");
        }

        String upperSQL = sql.toUpperCase();

        // Check for CREATE DATABASE command (non-standard SQLite, but useful for users)
        if (upperSQL.startsWith("CREATE DATABASE")) {
            return handleCreateDatabase(sql);
        }

        // Check for DROP DATABASE command
        if (upperSQL.startsWith("DROP DATABASE")) {
            return handleDropDatabase(sql);
        }

        // Check for USE DATABASE command (switch database)
        if (upperSQL.startsWith("USE ")) {
            return handleUseDatabase(sql);
        }

        // Check for SHOW DATABASES command (list all database files)
        if (upperSQL.equals("SHOW DATABASES") || upperSQL.equals("SHOW DATABASES;")) {
            return handleShowDatabases();
        }

        // Check for SHOW TABLES command (list tables in current database)
        if (upperSQL.equals("SHOW TABLES") || upperSQL.equals("SHOW TABLES;")) {
            return handleShowTables();
        }

        // Check for SHOW COLUMNS command (describe table structure)
        if (upperSQL.startsWith("SHOW COLUMNS FROM ") || upperSQL.startsWith("DESC ") || upperSQL.startsWith("DESCRIBE ")) {
            return handleShowColumns(sql);
        }

        // Check for HELP command
        if (upperSQL.equals("HELP") || upperSQL.equals("HELP;")) {
            return handleHelp();
        }

        // Check for EXIT/QUIT commands
        if (upperSQL.equals("EXIT") || upperSQL.equals("EXIT;") || 
            upperSQL.equals("QUIT") || upperSQL.equals("QUIT;") || 
            upperSQL.equals("\\Q")) {
            
            QueryResult exitResult = new QueryResult(true, "Bye");
            exitResult.setShouldExitApp(true);
            return exitResult;
        }

        // Check if database is open for normal commands
        if (database == null || !database.isOpen()) {
            return new QueryResult(false, "ERROR: No database is open. Use 'CREATE DATABASE dbname;' or 'USE dbname;'");
        }

        long startTime = System.currentTimeMillis();

        try {
            // Determine command type by analyzing first keyword
            String commandType = getCommandType(sql);

            QueryResult result;
            if (isQueryCommand(commandType)) {
                // SELECT, PRAGMA, EXPLAIN - return data
                result = executeQuery(sql);
            } else {
                // CREATE, INSERT, UPDATE, DELETE, DROP - perform action
                result = executeAction(sql);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);
            return result;

        } catch (SQLiteException e) {
            return new QueryResult(false, "ERROR: " + e.getMessage());
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: " + e.getMessage());
        }
    }

    /**
     * Handles USE DATABASE command to switch between databases.
     * Syntax: USE dbname; or USE dbname
     * Complexity: O(1)
     */
    private QueryResult handleUseDatabase(String sql) {
        try {
            String dbNamePart = sql.substring(4).trim(); // "USE ".length() = 4
            
            if (dbNamePart.endsWith(";")) {
                dbNamePart = dbNamePart.substring(0, dbNamePart.length() - 1).trim();
            }
            
            String dbName = dbNamePart;
            if (dbName.startsWith("\"") || dbName.startsWith("'")) {
                dbName = dbName.substring(1, dbName.length() - 1);
            }
            
            if (!dbName.toLowerCase().endsWith(".db")) {
                dbName = dbName + ".db";
            }
            
            boolean success = openOrCreateDatabase(dbName);
            if (success) {
                return new QueryResult(true, "Switched to database '" + dbName + "'");
            } else {
                return new QueryResult(false, "ERROR: Failed to open database '" + dbName + "'");
            }
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Invalid USE syntax. Use: USE dbname;");
        }
    }

    /**
     * Handles SHOW DATABASES command.
     * Lists all database files in the app's database directory.
     * Complexity: O(D) where D = number of database files
     */
    private QueryResult handleShowDatabases() {
        try {
            File dbDir = context.getDatabasePath("dummy").getParentFile();
            File[] dbFiles = dbDir.listFiles((dir, name) -> name.endsWith(".db"));
            
            List<String> columnNames = new ArrayList<>();
            columnNames.add("Database");
            
            List<List<String>> rows = new ArrayList<>();
            if (dbFiles != null && dbFiles.length > 0) {
                for (File dbFile : dbFiles) {
                    List<String> row = new ArrayList<>();
                    row.add(dbFile.getName().replace(".db", "")); // Show cleanly without extension
                    rows.add(row);
                }
            }
            
            String message = "Found " + rows.size() + " database(s)";
            return new QueryResult(true, message, columnNames, rows);
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Failed to list databases");
        }
    }

    /**
     * Handles SHOW TABLES command.
     * Lists all tables in the current database.
     * Complexity: O(T) where T = number of tables
     */
    private QueryResult handleShowTables() {
        if (database == null || !database.isOpen()) {
            return new QueryResult(false, "ERROR: No database is open");
        }

        try {
            List<String> columnNames = new ArrayList<>();
            columnNames.add("Tables_in_" + currentDatabaseName);
            
            List<List<String>> rows = new ArrayList<>();
            List<String> tables = getTableNames();
            
            for (String table : tables) {
                List<String> row = new ArrayList<>();
                row.add(table);
                rows.add(row);
            }
            
            String message = "Found " + rows.size() + " table(s)";
            return new QueryResult(true, message, columnNames, rows);
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Failed to list tables");
        }
    }

    /**
     * Handles SHOW COLUMNS FROM table_name or DESC/DESCRIBE table_name.
     * Shows table structure (column names, types, etc.)
     * Complexity: O(C) where C = number of columns
     */
    private QueryResult handleShowColumns(String sql) {
        if (database == null || !database.isOpen()) {
            return new QueryResult(false, "ERROR: No database is open");
        }

        try {
            String upperSQL = sql.toUpperCase();
            String tableName = "";
            
            if (upperSQL.startsWith("SHOW COLUMNS FROM ")) {
                tableName = sql.substring(18).trim();
            } else if (upperSQL.startsWith("DESC ")) {
                tableName = sql.substring(5).trim();
            } else if (upperSQL.startsWith("DESCRIBE ")) {
                tableName = sql.substring(9).trim();
            }
            
            // Remove semicolon and quotes
            tableName = tableName.replace(";", "").trim();
            if (tableName.startsWith("\"") || tableName.startsWith("'")) {
                tableName = tableName.substring(1, tableName.length() - 1);
            }
            
            // Use PRAGMA table_info to get column information
            String pragmaSQL = "PRAGMA table_info(" + tableName + ")";
            return executeQuery(pragmaSQL);
            
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Invalid SHOW COLUMNS syntax. Use: SHOW COLUMNS FROM tablename;");
        }
    }

    /**
     * Handles HELP command.
     * Lists all supported SQL commands.
     * Complexity: O(1)
     */
    /**
     * Handles HELP command.
     * Lists categorized SQL commands from Reference Helper.
     * Complexity: O(C*I) where C=categories, I=items
     */
    private QueryResult handleHelp() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Category");
        columnNames.add("Command");
        columnNames.add("Description");
        
        List<List<String>> rows = new ArrayList<>();
        
        // Standard Commands
        addHelpRow(rows, "Basic", "CREATE DATABASE", "Creates a new database");
        addHelpRow(rows, "Basic", "DROP DATABASE", "Deletes a database");
        addHelpRow(rows, "Basic", "SHOW DATABASES", "Lists all databases");
        addHelpRow(rows, "Basic", "USE dbname", "Switches to database");
        addHelpRow(rows, "Basic", "SHOW TABLES", "Lists tables in database");
        addHelpRow(rows, "Basic", "DESC tablename", "Shows table structure");
        
        // Add categories from Reference Helper
        Map<String, List<String[]>> categories = com.smartqueue.droidsql.utils.SQLReferenceHelper.getAllCategories();
        
        for (Map.Entry<String, List<String[]>> entry : categories.entrySet()) {
            String category = entry.getKey();
            for (String[] item : entry.getValue()) {
                addHelpRow(rows, category, item[0], item[1]);
            }
        }
        
        return new QueryResult(true, "PocketSQL Command Reference:", columnNames, rows);
    }

    private void addHelpRow(List<List<String>> rows, String category, String command, String description) {
        List<String> row = new ArrayList<>();
        row.add(category);
        row.add(command);
        row.add(description);
        rows.add(row);
    }


    /**
     * Handles CREATE DATABASE command.
     * Syntax: CREATE DATABASE dbname; or CREATE DATABASE dbname
     * 
     * This is a custom command (not standard SQLite) that makes the terminal
     * work like MySQL/PostgreSQL where databases can be created via SQL.
     * 
     * Complexity: O(1)
     */
    private QueryResult handleCreateDatabase(String sql) {
        try {
            // Remove "CREATE DATABASE" prefix
            String dbNamePart = sql.substring(15).trim(); // "CREATE DATABASE".length() = 15
            
            // Remove trailing semicolon if present
            if (dbNamePart.endsWith(";")) {
                dbNamePart = dbNamePart.substring(0, dbNamePart.length() - 1).trim();
            }
            
            // Extract database name (handle quotes if present)
            String dbName = dbNamePart;
            if (dbName.startsWith("\"") || dbName.startsWith("'")) {
                dbName = dbName.substring(1, dbName.length() - 1);
            }
            
            // Add .db extension if not present
            if (!dbName.toLowerCase().endsWith(".db")) {
                dbName = dbName + ".db";
            }
            
            // Validate database name
            if (dbName.isEmpty() || dbName.equals(".db")) {
                return new QueryResult(false, "ERROR: Invalid database name");
            }
            
            // Create/open the database
            boolean success = openOrCreateDatabase(dbName);
            
            if (success) {
                return new QueryResult(true, "Database '" + dbName + "' created and opened successfully");
            } else {
                return new QueryResult(false, "ERROR: Failed to create database '" + dbName + "'");
            }
            
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Invalid CREATE DATABASE syntax. Use: CREATE DATABASE dbname;");
        }
    }

    /**
     * Handles DROP DATABASE command.
     * Syntax: DROP DATABASE dbname;
     * Complexity: O(1) - file deletion
     */
    private QueryResult handleDropDatabase(String sql) {
        try {
            // Remove "DROP DATABASE" prefix
            String dbNamePart = sql.substring(13).trim(); // "DROP DATABASE".length() = 13
            
            // Remove trailing semicolon
            if (dbNamePart.endsWith(";")) {
                dbNamePart = dbNamePart.substring(0, dbNamePart.length() - 1).trim();
            }
            
            // Extract name
            String dbName = dbNamePart;
            if (dbName.startsWith("\"") || dbName.startsWith("'")) {
                dbName = dbName.substring(1, dbName.length() - 1);
            }
            
            if (!dbName.toLowerCase().endsWith(".db")) {
                dbName = dbName + ".db";
            }
            
            // Prevent deleting active database without warning (optional, but good practice)
            // For now, we allow it but must close it first if it's open
            if (currentDatabaseName != null && currentDatabaseName.equals(dbName)) {
                closeDatabase();
                currentDatabaseName = null;
            }
            
            File dbFile = context.getDatabasePath(dbName);
            boolean deleted = false;
            
            if (dbFile.exists()) {
                deleted = context.deleteDatabase(dbName);
            } else {
                return new QueryResult(false, "ERROR: Database '" + dbName + "' does not exist");
            }
            
            if (deleted) {
                return new QueryResult(true, "Database '" + dbName + "' dropped successfully");
            } else {
                return new QueryResult(false, "ERROR: Failed to drop database '" + dbName + "'");
            }
            
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Invalid DROP DATABASE syntax. Use: DROP DATABASE dbname;");
        }
    }

    /**
     * Executes a query command that returns data.
     * Complexity: O(N*M) for converting cursor to result list
     */
    private QueryResult executeQuery(String sql) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);

            // Extract column names - O(M) where M = column count
            String[] columnNamesArray = cursor.getColumnNames();
            List<String> columnNames = new ArrayList<>();
            for (String name : columnNamesArray) {
                columnNames.add(name);
            }

            // Extract rows - O(N*M) where N = row count, M = column count
            List<List<String>> rows = new ArrayList<>();
            while (cursor.moveToNext()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String value = cursor.getString(i);
                    row.add(value != null ? value : "NULL");
                }
                rows.add(row);
            }

            String message = "Query returned " + rows.size() + " row(s)";
            return new QueryResult(true, message, columnNames, rows);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Executes an action command (INSERT, UPDATE, DELETE, CREATE, etc.).
     * Complexity: O(1) to O(N) depending on command and indexes
     */
    private QueryResult executeAction(String sql) {
        // Sanitize SQL to support MySQL syntax
        String safeSql = sanitizeQuery(sql);
        database.execSQL(safeSql);
        return new QueryResult(true, "Command executed successfully");
    }

    /**
     * Sanitizes SQL query to make MySQL syntax compatible with SQLite.
     * Complexity: O(L) where L = length of SQL string
     */
    /**
     * Sanitizes SQL query to make MySQL syntax compatible with SQLite.
     * Complexity: O(L) where L = length of SQL string
     */
    private String sanitizeQuery(String sql) {
        String processedSql = sql;
        
        // 1. Handle MySQL AUTO_INCREMENT -> SQLite AUTOINCREMENT
        // SQLite Requirement: Must be "INTEGER PRIMARY KEY AUTOINCREMENT"
        // Pattern: "INT AUTO_INCREMENT PRIMARY KEY" -> "INTEGER PRIMARY KEY AUTOINCREMENT"
        if (processedSql.toUpperCase().contains("AUTO_INCREMENT")) {
            // Case 1: INT AUTO_INCREMENT PRIMARY KEY -> INTEGER PRIMARY KEY AUTOINCREMENT
            processedSql = processedSql.replaceAll("(?i)\\bINT\\s+AUTO_INCREMENT\\s+PRIMARY\\s+KEY\\b", "INTEGER PRIMARY KEY AUTOINCREMENT");
            
            // Case 2: INTEGER AUTO_INCREMENT PRIMARY KEY -> INTEGER PRIMARY KEY AUTOINCREMENT (just in case)
            processedSql = processedSql.replaceAll("(?i)\\bINTEGER\\s+AUTO_INCREMENT\\s+PRIMARY\\s+KEY\\b", "INTEGER PRIMARY KEY AUTOINCREMENT");
            
            // Case 3: Just AUTO_INCREMENT -> AUTOINCREMENT (Fallback)
            // Note: This might fail if used with INT instead of INTEGER, but catches other variants
            processedSql = processedSql.replaceAll("(?i)AUTO_INCREMENT", "AUTOINCREMENT");
        }
        
        // 2. Handle ENUM types (MySQL) -> TEXT (SQLite)
        // Pattern: "gender ENUM('Male', 'Female')" -> "gender TEXT CHECK(gender IN ('Male', 'Female'))"
        // Simplified: Just replace ENUM(...) with TEXT.
        // Regex: Matches "ENUM\s*\(... content ...\)"
        if (processedSql.toUpperCase().contains("ENUM")) {
             processedSql = processedSql.replaceAll("(?i)\\bENUM\\s*\\([^)]+\\)", "TEXT");
        }

        // 3. Remove MySQL engine declaration if present (e.g. ENGINE=InnoDB)
        if (processedSql.toUpperCase().contains("ENGINE=")) {
            processedSql = processedSql.replaceAll("(?i)ENGINE\\s*=\\s*\\w+", "");
        }
        
        // 4. Handle unsigned int (SQLite doesn't strictly enforce but we can clean it)
        processedSql = processedSql.replaceAll("(?i)UNSIGNED", "");
        
        // 5. Handle TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        // SQLite supports this, but sometimes needs "DATETIME DEFAULT CURRENT_TIMESTAMP"
        // We generally leave it unless specific issues arise, but "TIMESTAMP" affinity is usually NUMERIC in SQLite.
        // Let's rely on SQLite's flexible typing.

        // 6. Handle TRUNCATE TABLE (MySQL) -> DELETE FROM (SQLite)
        if (processedSql.toUpperCase().startsWith("TRUNCATE")) {
            processedSql = processedSql.replaceAll("(?i)TRUNCATE\\s+TABLE", "DELETE FROM");
            processedSql = processedSql.replaceAll("(?i)TRUNCATE", "DELETE FROM");
        }

        // 7. Handle NOW() (MySQL) -> CURRENT_TIMESTAMP (SQLite)
        if (processedSql.toUpperCase().contains("NOW()")) {
            processedSql = processedSql.replaceAll("(?i)NOW\\(\\)", "CURRENT_TIMESTAMP");
        }

        // 8. Handle # Comments (MySQL) -> -- Comments (SQLite)
        if (processedSql.contains("#")) {
            processedSql = processedSql.replaceAll("^#", "--"); 
            processedSql = processedSql.replaceAll("\\s+#", " --"); 
        }

        // 9. Handle INSERT IGNORE (MySQL) -> INSERT OR IGNORE (SQLite)
        if (processedSql.toUpperCase().contains("INSERT IGNORE")) {
            processedSql = processedSql.replaceAll("(?i)INSERT\\s+IGNORE", "INSERT OR IGNORE");
        }

        // 10. Strip MySQL CREATE TABLE Extras
        // Remove DEFAULT CHARSET=..., COLLATE=..., ROW_FORMAT=..., COMMENT=...
        if (processedSql.toUpperCase().contains("CREATE TABLE")) {
            processedSql = processedSql.replaceAll("(?i)DEFAULT\\s+CHARSET\\s*=\\s*\\w+", "");
            processedSql = processedSql.replaceAll("(?i)COLLATE\\s*=\\s*\\w+", "");
            processedSql = processedSql.replaceAll("(?i)ROW_FORMAT\\s*=\\s*\\w+", "");
            processedSql = processedSql.replaceAll("(?i)COMMENT\\s*=\\s*'.*?'", ""); // Simple comment removal
            processedSql = processedSql.replaceAll("(?i)AUTO_INCREMENT\\s*=\\s*\\d+", ""); // Table level auto_increment
            processedSql = processedSql.replaceAll("(?i)CHECKSUM\\s*=\\s*\\d+", "");
        }

        // 11. Strip Optimization/Priority Keywords
        processedSql = processedSql.replaceAll("(?i)\\bSQL_CALC_FOUND_ROWS\\b", "");
        processedSql = processedSql.replaceAll("(?i)\\bSQL_NO_CACHE\\b", "");
        processedSql = processedSql.replaceAll("(?i)\\bHIGH_PRIORITY\\b", "");
        processedSql = processedSql.replaceAll("(?i)\\bLOW_PRIORITY\\b", "");
        processedSql = processedSql.replaceAll("(?i)\\bDELAYED\\b", "");
        processedSql = processedSql.replaceAll("(?i)\\bQUICK\\b", "");

        // 12. Strip Locking
        if (processedSql.toUpperCase().startsWith("LOCK TABLES") || processedSql.toUpperCase().startsWith("UNLOCK TABLES")) {
            // Locking is not supported (or needed) in this single-user mode, convert to no-op comment
            return "-- " + processedSql; 
        }

        // 13. Strip Partitioning
        if (processedSql.toUpperCase().contains("PARTITION BY")) {
            // Regex to remove PARTITION BY up to end of statement is hard. 
            // Simple approach: Strip basic partition header, might fail complex nesting.
            // For now, assume it's at the end of CREATE TABLE.
             processedSql = processedSql.replaceAll("(?i)PARTITION\\s+BY\\s+.*", ";");
        }

        return processedSql;
    }


    /**
     * Determines the SQL command type from the first keyword.
     * Complexity: O(1)
     */
    private String getCommandType(String sql) {
        String upperSQL = sql.toUpperCase().trim();
        String[] parts = upperSQL.split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }
        return "";
    }

    /**
     * Checks if the command returns data.
     * Complexity: O(1)
     */
    private boolean isQueryCommand(String commandType) {
        return commandType.equals("SELECT") || 
               commandType.equals("PRAGMA") || 
               commandType.equals("EXPLAIN");
    }

    /**
     * Exports current database to Downloads folder.
     * Complexity: O(N) where N = database file size
     */
    public boolean exportDatabase() {
        if (currentDatabaseName == null) {
            return false;
        }

        File currentDB = context.getDatabasePath(currentDatabaseName);
        if (!currentDB.exists()) {
            return false;
        }

        try {
            // Android 10 (Q) and above: Use MediaStore (Scoped Storage)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, currentDatabaseName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream"); // SQLite is binary
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri uri = context.getContentResolver().insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    return false;
                }

                try (java.io.InputStream in = new FileInputStream(currentDB);
                     java.io.OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                return true;
            } 
            // Older Android: Use Legacy File I/O (requires WRITE_EXTERNAL_STORAGE)
            else {
                File exportDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                File exportDB = new File(exportDir, currentDatabaseName);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(exportDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Closes the current database connection.
     * Complexity: O(1)
     */
    public void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    /**
     * Checks if a database is currently open.
     * Complexity: O(1)
     */
    public boolean isDatabaseOpen() {
        return database != null && database.isOpen();
    }

    /**
     * Gets the current database name.
     * Complexity: O(1)
     */
    public String getCurrentDatabaseName() {
        return currentDatabaseName;
    }

    /**
     * Gets list of all tables in current database.
     * Useful for learning and exploration.
     * Complexity: O(T) where T = number of tables
     */
    public List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        if (database == null || !database.isOpen()) {
            return tables;
        }

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN ('android_metadata', 'sqlite_sequence') ORDER BY name", 
                null);
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tables;
    }
}
