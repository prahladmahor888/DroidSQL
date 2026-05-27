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
            
            // AUTO-ATTACH other databases to allow "SELECT * FROM db.table" syntax
            // This enables cross-database joins and querying tables in other DBs
            attachAvailableDatabases(database, dbName);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Attaches all other databases in the directory to the current session.
     * Enables accessing tables like 'world.city' or 'ecommerce.products'.
     */
    private void attachAvailableDatabases(SQLiteDatabase db, String currentDbName) {
        try {
            // Get database directory
            File dummy = context.getDatabasePath("dummy");
            File dbDir = dummy.getParentFile();
            if (dbDir == null || !dbDir.exists()) return;

            File[] dbFiles = dbDir.listFiles((dir, name) -> name.endsWith(".db"));
            
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    String fileName = file.getName();
                    if (fileName.equalsIgnoreCase(currentDbName)) {
                        continue; // Skip current DB (it's main)
                    }
                    
                    String dbAlias = fileName.replace(".db", "");
                    // Sanitize alias: remove non-alphanumeric chars to prevent injection
                    dbAlias = dbAlias.replaceAll("[^a-zA-Z0-9_]", "");
                    
                    if (dbAlias.isEmpty()) continue;

                    try {
                        String attachCmd = "ATTACH DATABASE '" + file.getAbsolutePath() + "' AS " + dbAlias;
                        db.execSQL(attachCmd);
                    } catch (Exception e) {
                        // Ignore attach failures (e.g. already attached or limit reached)
                        // Log.e("DroidSQL", "Failed to attach " + fileName, e);
                    }
                }
            }
        } catch (Exception e) {
            // Log.e("DroidSQL", "Error listing DBs for attach", e);
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

        // Normalize for easier matching: remove semicolon and trim
        String cleanUpper = upperSQL.replace(";", "").trim();

        // Check for SUGGEST command
        if (cleanUpper.startsWith("SUGGEST")) {
            return handleSuggest(sql);
        }

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
        // cleanUpper allows: "SHOW DATABASES", "show databases;", "SHOW DATABASES   ;", etc.
        if (cleanUpper.equals("SHOW DATABASES")) {
            return handleShowDatabases();
        }

        // Check for SHOW TABLES command (list tables in current database)
        // Supports: SHOW TABLES, SHOW TABLES FROM db, SHOW TABLES IN db
        if (cleanUpper.startsWith("SHOW TABLES")) {
            // Check for FROM/IN clause
            String[] parts = sql.trim().split("\\s+");
            if (parts.length >= 4 && (parts[2].equalsIgnoreCase("FROM") || parts[2].equalsIgnoreCase("IN"))) {
                String targetDb = parts[3].replace(";", "");
                return handleShowTables(targetDb);
            }
            return handleShowTables(null);
        }

        // Check for SHOW COLUMNS command (describe table structure)
        if (upperSQL.startsWith("SHOW COLUMNS FROM ") || upperSQL.startsWith("DESC ") || upperSQL.startsWith("DESCRIBE ")) {
            return handleShowColumns(sql);
        }

        // Check for SHOW CREATE TABLE command (MySQL compatibility)
        if (upperSQL.startsWith("SHOW CREATE TABLE ")) {
            return handleShowCreateTable(sql);
        }

        // Check for HELP command
        if (cleanUpper.equals("HELP")) {
            return handleHelp();
        }

        // Check for EXIT/QUIT commands
        if (cleanUpper.equals("EXIT") || cleanUpper.equals("QUIT") || cleanUpper.equals("\\Q")) {
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
            } else if (upperSQL.startsWith("ALTER TABLE") && upperSQL.contains("ADD CONSTRAINT")) {
                 // Check for ALTER TABLE ADD CONSTRAINT emulation
                 result = handleAlterTableAddConstraint(sql);
            } else if (upperSQL.startsWith("ALTER TABLE") && upperSQL.contains("DROP CONSTRAINT")) {
                 // Check for ALTER TABLE DROP CONSTRAINT emulation
                 result = handleAlterTableDropConstraint(sql);
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
     * Handles ALTER TABLE ADD CONSTRAINT logic by emulating it in SQLite.
     * Strategy: Rename original -> Create new with constraint -> Copy data -> Drop original.
     * Complexity: O(N) data copy
     */
    private QueryResult handleAlterTableAddConstraint(String sql) {
        // Syntax: ALTER TABLE tableName ADD CONSTRAINT constraintName CHECK (condition)
        String upper = sql.toUpperCase();
        try {
            // 1. Parse Table Name
            // Remove ALTER TABLE
            String temp = sql.substring(11).trim(); 
            // Find end of table name (before ADD CONSTRAINT)
            int addIndex = temp.toUpperCase().indexOf("ADD CONSTRAINT");
            if (addIndex == -1) return new QueryResult(false, "ERROR: Invalid syntax. Use ALTER TABLE t ADD CONSTRAINT c CHECK (...)");
            
            String tableName = temp.substring(0, addIndex).trim();
            // Handle quotes
            if (tableName.startsWith("'") || tableName.startsWith("\"") || tableName.startsWith("`")) {
                tableName = tableName.substring(1, tableName.length()-1);
            }

            // 2. Parse Constraint Definition
            String constraintDef = temp.substring(addIndex + 14).trim(); // Skip "ADD CONSTRAINT"
            // Expect: constraintName CHECK (condition)
            if (constraintDef.endsWith(";")) constraintDef = constraintDef.substring(0, constraintDef.length()-1);

            // 3. Get existing CREATE SQL
            Cursor c = database.rawQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            if (!c.moveToFirst()) {
                c.close();
                return new QueryResult(false, "ERROR: Table '" + tableName + "' not found");
            }
            String oldCreateSql = c.getString(0);
            c.close();

            // 4. Construct New CREATE SQL
            // Remove closing parenthesis
            int lastParen = oldCreateSql.lastIndexOf(")");
            if (lastParen == -1) return new QueryResult(false, "ERROR: Could not parse table schema");
            
            String newCreateSql = oldCreateSql.substring(0, lastParen) + 
                                  ", CONSTRAINT " + constraintDef + 
                                  ")";
                                  
            // 5. Execute Migration Transaction
            database.beginTransaction();
            try {
                // A. Rename Old
                String tempTableName = tableName + "_old_migration_" + System.currentTimeMillis();
                database.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tempTableName);
                
                // B. Create New
                database.execSQL(newCreateSql);
                
                // C. Copy Data
                database.execSQL("INSERT INTO " + tableName + " SELECT * FROM " + tempTableName);
                
                // D. Drop Old
                database.execSQL("DROP TABLE " + tempTableName);
                
                database.setTransactionSuccessful();
                return new QueryResult(true, "Constraint added successfully (Table rebuilt)");
            } finally {
                database.endTransaction();
            }

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("SQLITE_CONSTRAINT_CHECK")) {
                return new QueryResult(false, "Action Aborted: Existing data violates the new constraint.\nPlease update your data to satisfy the check or modify the constraint.");
            } else if (msg != null && msg.contains("SQLITE_CONSTRAINT")) {
                 return new QueryResult(false, "Action Aborted: Constraint violation (Unique/Foreign Key) in existing data.");
            }
            return new QueryResult(false, "ERROR: Migration failed: " + e.getMessage());
        }
    }

    /**
     * Handles ALTER TABLE DROP CONSTRAINT logic.
     * Emulates DROP CONSTRAINT by recreating the table without the constraint.
     */
    private QueryResult handleAlterTableDropConstraint(String sql) {
        // Syntax: ALTER TABLE tableName DROP CONSTRAINT constraintName
        try {
            String upper = sql.toUpperCase();
            // 1. Parse Table Name
            String temp = sql.substring(11).trim(); // Remove ALTER TABLE
            int dropIndex = temp.toUpperCase().indexOf("DROP CONSTRAINT");
            if (dropIndex == -1) return new QueryResult(false, "ERROR: Invalid syntax");

            String tableName = temp.substring(0, dropIndex).trim();
            if (tableName.startsWith("'") || tableName.startsWith("\"") || tableName.startsWith("`")) {
                tableName = tableName.substring(1, tableName.length()-1);
            }

            // 2. Parse Constraint Name
            String constraintName = temp.substring(dropIndex + 15).trim();
            if (constraintName.endsWith(";")) constraintName = constraintName.substring(0, constraintName.length()-1);
            if (constraintName.startsWith("'") || constraintName.startsWith("\"") || constraintName.startsWith("`")) {
                constraintName = constraintName.substring(1, constraintName.length()-1);
            }

            // 3. Get existing Schema
            Cursor c = database.rawQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            if (!c.moveToFirst()) {
                c.close();
                return new QueryResult(false, "ERROR: Table '" + tableName + "' not found");
            }
            String oldCreateSql = c.getString(0);
            c.close();

            // 4. Remove Constraint
            // Look for "CONSTRAINT constraintName ..."
            String newCreateSql = oldCreateSql;
            boolean found = false;

            // Pattern 1: Regex matches strict CONSTRAINT definition with params
            // Matches: , CONSTRAINT name TYPE ( ... )
            // We use Pattern.quote to handle regex chars in constraint name
            String pattern1 = "(?i),\\s*CONSTRAINT\\s+" + java.util.regex.Pattern.quote(constraintName) + "\\s+(CHECK|FOREIGN\\s+KEY|PRIMARY\\s+KEY|UNIQUE)\\s*\\([^)]+\\)";
            
            java.util.regex.Matcher m1 = java.util.regex.Pattern.compile(pattern1).matcher(oldCreateSql);
            if (m1.find()) {
                newCreateSql = m1.replaceFirst("");
                found = true;
            } else {
                 // Fallback: If regex failed (e.g. nested parens or weird formatting), try simple removal
                 // This assumes the user used our ADD CONSTRAINT tool which appends typical syntax.
                 // We will look for explicit substring ", CONSTRAINT name CHECK" 
                 // and remove until the next comma or closing paren.
                 // This is risky but covers 90% of cases for this emulator.
                 
                 String searchKey = "CONSTRAINT " + constraintName;
                 int idx = oldCreateSql.toUpperCase().indexOf(searchKey.toUpperCase());
                 if (idx != -1) {
                     // Check if preceded by comma
                     int startCut = idx;
                     int commaIdx = oldCreateSql.lastIndexOf(",", idx);
                     if (commaIdx != -1 && oldCreateSql.substring(commaIdx, idx).trim().isEmpty()) {
                         startCut = commaIdx;
                     }
                     
                     // Find end
                     // Scan for closing paren of the constraint definition
                     // We assume balanced parens
                     int open = 0;
                     int endCut = -1;
                     for (int i = idx; i < oldCreateSql.length(); i++) {
                         char ch = oldCreateSql.charAt(i);
                         if (ch == '(') open++;
                         else if (ch == ')') {
                             open--;
                             if (open == 0 && i > idx + searchKey.length()) { // Ensure we passed header
                                 // This might be the end of "CHECK (...)"
                                 endCut = i + 1; // Include closing paren
                                 break;
                             }
                         }
                     }
                     
                     if (endCut != -1) {
                         newCreateSql = oldCreateSql.substring(0, startCut) + oldCreateSql.substring(endCut);
                         found = true;
                     }
                 }
            }

            if (!found) {
                return new QueryResult(false, "ERROR: Constraint '" + constraintName + "' not found or could not be parsed.");
            }

            // 5. Execute Migration
            database.beginTransaction();
            try {
                String tempTableName = tableName + "_old_" + System.currentTimeMillis();
                database.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tempTableName);
                database.execSQL(newCreateSql);
                database.execSQL("INSERT INTO " + tableName + " SELECT * FROM " + tempTableName);
                database.execSQL("DROP TABLE " + tempTableName);
                database.setTransactionSuccessful();
                return new QueryResult(true, "Constraint '" + constraintName + "' dropped successfully");
            } finally {
                database.endTransaction();
            }

        } catch (Exception e) {
            return new QueryResult(false, "ERROR: Drop constraint failed: " + e.getMessage());
        }
    }

    /**
     * Handles SHOW CREATE TABLE command.
     * Mimics MySQL output by querying sqlite_master.
     * Complexity: O(1) - single lookup
     */
    private QueryResult handleShowCreateTable(String sql) {
        try {
            // Remove "SHOW CREATE TABLE " prefix
            String tableName = sql.trim().substring(18).trim(); 
            if (tableName.endsWith(";")) {
                tableName = tableName.substring(0, tableName.length() - 1).trim();
            }
            // Remove potential quotes
            if (tableName.startsWith("'") || tableName.startsWith("\"") || tableName.startsWith("`")) {
                tableName = tableName.substring(1, tableName.length() - 1);
            }

            Cursor cursor = database.rawQuery(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name=?", 
                new String[]{tableName});

            if (cursor.moveToFirst()) {
                String createSql = cursor.getString(0);
                cursor.close();

                List<String> columns = new ArrayList<>();
                columns.add("Table");
                columns.add("Create Table");

                List<List<String>> rows = new ArrayList<>();
                List<String> row = new ArrayList<>();
                row.add(tableName);
                row.add(createSql);
                rows.add(row);

                return new QueryResult(true, "Table definition:", columns, rows);
            } else {
                cursor.close();
                return new QueryResult(false, "ERROR: Table '" + tableName + "' does not exist");
            }
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: " + e.getMessage());
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
     * Supports current database or specific database (FROM/IN clause).
     * Complexity: O(T) where T = number of tables
     */
    private QueryResult handleShowTables(String targetDb) {
        SQLiteDatabase dbToUse = database;
        boolean isTempConnection = false;
        String dbNameForDisplay = currentDatabaseName;

        // If target database specified, try to open it
        if (targetDb != null) {
            File dbFile = context.getDatabasePath(targetDb.endsWith(".db") ? targetDb : targetDb + ".db");
            if (!dbFile.exists()) {
                return new QueryResult(false, "Database '" + targetDb + "' does not exist");
            }
            try {
                // Open read-only connection to target DB
                dbToUse = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                isTempConnection = true;
                dbNameForDisplay = targetDb;
            } catch (SQLiteException e) {
                return new QueryResult(false, "Failed to open database '" + targetDb + "': " + e.getMessage());
            }
        } else {
            // Use current database
            if (database == null || !database.isOpen()) {
                return new QueryResult(false, "ERROR: No database is open");
            }
        }

        Cursor cursor = null;
        try {
            List<String> tables = new ArrayList<>();
            cursor = dbToUse.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' ORDER BY name", null);
            
            if (cursor.moveToFirst()) {
                do {
                    tables.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            // Cursor closed in finally block or explicity here? Better to close here before generating result.
            cursor.close();
            cursor = null;

            // Format result as table
            List<String> columns = new ArrayList<>();
            columns.add("Tables_in_" + dbNameForDisplay);
            
            List<List<String>> rows = new ArrayList<>();
            for (String table : tables) {
                List<String> row = new ArrayList<>();
                row.add(table);
                rows.add(row);
            }

            return new QueryResult(true, "Found " + tables.size() + " tables", columns, rows);

        } catch (Exception e) {
            return new QueryResult(false, "Error listing tables: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (isTempConnection && dbToUse != null) {
                dbToUse.close();
            }
        }
    }

    /**
     * Handles SHOW COLUMNS FROM table_name or DESC/DESCRIBE table_name.
     * Shows table structure in MySQL-compatible format.
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
            
            // Get raw column info from SQLite
            //noinspection SqlSourceToSinkFlow,SqlResolve,SqlDialectInspection
            String pragmaSQL = "PRAGMA table_info(" + tableName + ")";
            Cursor cursor = database.rawQuery(pragmaSQL, null);
            
            // Build MySQL-compatible result with columns: Field, Type, Null, Key, Default, Extra
            List<String> columnNames = new ArrayList<>();
            columnNames.add("Field");
            columnNames.add("Type");
            columnNames.add("Null");
            columnNames.add("Key");
            columnNames.add("Default");
            columnNames.add("Extra");
            
            List<List<String>> rows = new ArrayList<>();
            
            while (cursor.moveToNext()) {
                List<String> row = new ArrayList<>();
                
                // Field (column name)
                String fieldName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                row.add(fieldName);
                
                // Type (data type - uppercase for MySQL compatibility)
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                if (type == null || type.isEmpty()) type = "TEXT";
                row.add(type.toLowerCase());
                
                // Null (YES or NO)
                int notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull"));
                row.add(notNull == 1 ? "NO" : "YES");
                
                // Key (PRI for primary key, UNI for unique, MUL for foreign key indexes, empty otherwise)
                int isPrimaryKey = cursor.getInt(cursor.getColumnIndexOrThrow("pk"));
                String keyType = "";
                if (isPrimaryKey > 0) {
                    keyType = "PRI";
                } else {
                    // Check for unique constraints via index info
                    //noinspection SqlSourceToSinkFlow,SqlResolve,SqlDialectInspection
                    Cursor indexCursor = database.rawQuery(
                        "SELECT il.name FROM pragma_index_list('" + tableName + "') il " +
                        "JOIN pragma_index_info(il.name) ii ON 1=1 " +
                        "WHERE ii.name = ? AND il.\"unique\" = 1", 
                        new String[]{fieldName});
                    if (indexCursor.moveToFirst()) {
                        keyType = "UNI";
                    }
                    indexCursor.close();
                    
                    // Check for foreign keys
                    if (keyType.isEmpty()) {
                        //noinspection SqlSourceToSinkFlow,SqlResolve,SqlDialectInspection
                        Cursor fkCursor = database.rawQuery(
                            "SELECT * FROM pragma_foreign_key_list('" + tableName + "') WHERE \"from\" = ?", 
                            new String[]{fieldName});
                        if (fkCursor.moveToFirst()) {
                            keyType = "MUL";
                        }
                        fkCursor.close();
                    }
                }
                row.add(keyType);
                
                // Default (default value or NULL)
                String defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"));
                row.add(defaultValue == null ? "NULL" : defaultValue);
                
                // Extra (auto_increment for INTEGER PRIMARY KEY AUTOINCREMENT, DEFAULT_GENERATED for defaults)
                String extra = "";
                if (isPrimaryKey > 0 && type.toUpperCase().contains("INTEGER")) {
                    // Check if table has AUTOINCREMENT
                    Cursor schemaCursor = database.rawQuery(
                        "SELECT sql FROM sqlite_master WHERE type='table' AND name=?", 
                        new String[]{tableName});
                    if (schemaCursor.moveToFirst()) {
                        String createSQL = schemaCursor.getString(0);
                        if (createSQL != null && createSQL.toUpperCase().contains("AUTOINCREMENT")) {
                            extra = "auto_increment";
                        }
                    }
                    schemaCursor.close();
                } else if (defaultValue != null && !defaultValue.equals("NULL")) {
                    extra = "DEFAULT_GENERATED";
                }
                row.add(extra);
                
                rows.add(row);
            }
            cursor.close();
            
            String message = rows.size() + " row" + (rows.size() == 1 ? "" : "s") + " in set";
            return new QueryResult(true, message, columnNames, rows);
            
        } catch (Exception e) {
            return new QueryResult(false, "ERROR: " + e.getMessage());
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
        addHelpRow(rows, "Basic", "SUGGEST [key]", "Finds SQL templates");
        
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
     * Handles SUGGEST command.
     * Suggests SQL templates based on keywords.
     * Complexity: O(T) where T = number of templates
     */
    private QueryResult handleSuggest(String sql) {
        String query = sql.trim().substring(7).trim().toLowerCase(); // Remove "SUGGEST"
        
        StringBuilder message = new StringBuilder();
        
        // Special case: SUGGEST PERFORMANCE
        if (query.contains("perf")) {
            message.append("PERFORMANCE TIPS:\n");
            message.append(com.smartqueue.droidsql.utils.SQLTemplateHelper.getPerformanceTips());
            return new QueryResult(true, message.toString());
        }

        // Special case: SUGGEST DATATYPES
        if (query.contains("type") || query.contains("data")) {
             return new QueryResult(true, getMySQLDataTypesReference());
        }
        
        String[] names = com.smartqueue.droidsql.utils.SQLTemplateHelper.getTemplateNames();
        int foundCount = 0;
        
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String content = com.smartqueue.droidsql.utils.SQLTemplateHelper.getTemplate(i);
            
            // Search match in name or content (if query provided)
            boolean match = query.isEmpty() || name.toLowerCase().contains(query) || content.toLowerCase().contains(query);
            
            if (match) {
                foundCount++;
                message.append(String.format("--- %s ---\n", name));
                message.append(content).append("\n\n");
            }
        }
        
        if (foundCount == 0) {
            return new QueryResult(false, "No suggestions found for '" + query + "'. Try 'SUGGEST TABLE' or 'SUGGEST INSERT'");
        }
        
        // Use a header for the count
        String header = String.format("Found %d suggestion(s):\n\n", foundCount);
        return new QueryResult(true, header + message.toString());
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
        // Sanitize SQL to support MySQL syntax (e.g. VERSION(), NOW())
        sql = sanitizeQuery(sql);

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
        
        // 2. Handle ENUM types (MySQL) -> TEXT CHECK(...) (SQLite)
        // Pattern: "gender ENUM('Male', 'Female')" -> "gender TEXT CHECK(gender IN ('Male', 'Female'))"
        if (processedSql.toUpperCase().contains("ENUM")) {
            // Regex captures: Group 1 = Column Name, Group 2 = Enum Values ('A','B')
            // Note: This assumes simple definition "colName ENUM(...)"
            processedSql = processedSql.replaceAll("(?i)(\\w+)\\s+ENUM\\s*(\\([^)]+\\))", "$1 TEXT CHECK($1 IN $2)");
            
            // Fallback for standalone ENUM usage (less common, just type conversion)
            processedSql = processedSql.replaceAll("(?i)\\bENUM\\s*\\([^)]+\\)", "TEXT");
        }

        // 2b. Handle SET types (MySQL) -> TEXT (SQLite)
        if (processedSql.toUpperCase().contains("SET(")) {
             processedSql = processedSql.replaceAll("(?i)\\bSET\\s*\\([^)]+\\)", "TEXT");
        }
        
        // 2c. Handle SERIAL type (MySQL/PostgreSQL) -> INTEGER PRIMARY KEY AUTOINCREMENT
        if (processedSql.toUpperCase().contains("SERIAL")) {
            processedSql = processedSql.replaceAll("(?i)\\bSERIAL\\b", "INTEGER PRIMARY KEY AUTOINCREMENT");
            // Cleanup potential duplicate PRIMARY KEY definition if user typed "SERIAL PRIMARY KEY"
            processedSql = processedSql.replaceAll("(?i)PRIMARY\\s+KEY\\s+AUTOINCREMENT\\s+PRIMARY\\s+KEY", "PRIMARY KEY AUTOINCREMENT");
        }
        
        // 2d. Handle JSON type (MySQL) -> TEXT (SQLite)
        processedSql = processedSql.replaceAll("(?i)\\bJSON\\b", "TEXT");

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

        // 12b. Strip SPATIAL keyword (MySQL) -> Treat as normal KEY/INDEX
        processedSql = processedSql.replaceAll("(?i)\\bSPATIAL\\b", "");

        // 13. Strip Partitioning
        if (processedSql.toUpperCase().contains("PARTITION BY")) {
             processedSql = processedSql.replaceAll("(?i)PARTITION\\s+BY\\s+.*", ";");
        }
        
        // 14. Handle DELIMITER (MySQL) -> Ignore (SQLite)
        // Just strip the line or comment it out
        if (processedSql.toUpperCase().startsWith("DELIMITER")) {
            return "-- " + processedSql;
        }

        // 15. Handle SIGNAL SQLSTATE (MySQL) -> RAISE(ABORT, msg) (SQLite)
        // Syntax: SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error text';
        if (processedSql.toUpperCase().contains("SIGNAL SQLSTATE")) {
             // Regex to extract message text
             // Matches: SET MESSAGE_TEXT = '...'
             java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)SET\\s+MESSAGE_TEXT\\s*=\\s*'([^']+)'");
             java.util.regex.Matcher m = p.matcher(processedSql);
             if (m.find()) {
                 String msg = m.group(1);
                 // Replace entire SIGNAL statement with SELECT RAISE(ABORT, 'msg');
                 // Note: SIGNAL usually appears inside triggers.
                 // We need to replace the specific statement.
                 // Simplification: Replace the whole match "SIGNAL ... ;" 
                 // But regex is hard. Let's try to replace the whole line if it's simple.
                 
                 // Better approach: Replace "SIGNAL SQLSTATE ... 'msg'" with "SELECT RAISE(ABORT, 'msg')"
                 // But we need to handle the variable parts.
                 
                 // Strategy: 
                 // Replace "SIGNAL SQLSTATE '...'" -> "SELECT RAISE(ABORT,"
                 // Replace "SET MESSAGE_TEXT = " -> ""
                 // But syntax is messy.
                 
                 // Robust replacement for standard pattern:
                 processedSql = processedSql.replaceAll("(?i)SIGNAL\\s+SQLSTATE\\s+'[^']+'\\s+SET\\s+MESSAGE_TEXT\\s*=\\s*'([^']+)'", "SELECT RAISE(ABORT, '$1')");
             }
        }

        // 16. Handle VERSION() (MySQL) -> sqlite_version() (SQLite)
        if (processedSql.toUpperCase().contains("VERSION()")) {
            processedSql = processedSql.replaceAll("(?i)VERSION\\(\\)", "sqlite_version()");
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


    /**
     * Returns the comprehensive MySQL Data Types reference guide.
     */
    private String getMySQLDataTypesReference() {
        return "# MySQL Data Types – Quick Reference\n\n" +
                "## 1. Numeric Data Types\n" +
                "* **TINYINT** - 1 byte\n" +
                "* **SMALLINT** - 2 bytes\n" +
                "* **MEDIUMINT** - 3 bytes\n" +
                "* **INT / INTEGER** - 4 bytes\n" +
                "* **BIGINT** - 8 bytes\n" +
                "* **DECIMAL(M,D)** - Exact value (Money)\n" +
                "* **FLOAT / DOUBLE** - Approximate\n\n" +
                "## 2. Date and Time\n" +
                "* **DATE** - YYYY-MM-DD\n" +
                "* **TIME** - HH:MM:SS\n" +
                "* **DATETIME** - Mixed\n" +
                "* **TIMESTAMP** - Auto-updating\n" +
                "* **YEAR** - YYYY\n\n" +
                "## 3. String Types\n" +
                "* **CHAR(n)** - Fixed\n" +
                "* **VARCHAR(n)** - Variable\n" +
                "* **TEXT / LONGTEXT** - Large text\n" +
                "* **BLOB / LONGBLOB** - Binary data\n\n" +
                "## 4. Other Types\n" +
                "* **BOOLEAN** - True/False\n" +
                "* **ENUM('a','b')** - One of list\n" +
                "* **SET('a','b')** - Multiple of list\n" +
                "* **JSON** - Structured data\n" +
                "* **UUID** - Unique ID\n" +
                "* **GEOMETRY / POINT** - Spatial\n\n" +
                "Note: PocketSQL automatically maps these matching SQLite types!";
    }
}
