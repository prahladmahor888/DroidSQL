package com.smartqueue.droidsql.utils;

/**
 * Provides SQL template examples for learning and quick execution.
 * Helps users understand SQL syntax and test database operations.
 */
public class SQLTemplateHelper {

    public static final String[] TEMPLATE_NAMES = {
        "Create Database",
        "Show Databases",
        "Use Database",
        "Show Tables",
        "Create Table",
        "Create Table (Complex)",
        "Show Columns",
        "Insert Data",
        "Select All",
        "Select With Where",
        "Update Data",
        "Delete Data",
        "Drop Table",
        "Create Index",
        "Count Rows",
        "Alter Table Add",
        "Alter Table Rename",
        "Alter Table Rename Column",
        "Drop Table (Safe)",
        "Truncate Table",
        "Select Distinct",
        "Like / Wildcards",
        "Group By",
        "Having Clause",
        "Limit & Offset",
        "Inner Join",
        "Left Join",
        "Union",
        "Transaction",
        "Create View",
        "Date: Current Date",
        "Date: Date & Time",
        "Date: Format YYYY-MM-DD",
        "String: Upper Case",
        "String: Substring",
        "String: Concatenation",
        "Math: Average",
        "Math: Sum",
        "Math: Round",
        "Math: Min & Max",
        "Subquery (Nested)",
        "CASE Statement",
        "Show Views",
        "Show View Definition",
        "Use View (Select)",
        "Alter Table: Add Constraint",
        "Alter Table: Drop Constraint",
        "Create Spatial Index (R-Tree)"
    };

    public static final String[] TEMPLATES = {
        // Create Database
        "CREATE DATABASE mydb;",
        
        // Show Databases
        "SHOW DATABASES;",
        
        // Use Database
        "USE mydb;",
        
        // Show Tables
        "SHOW TABLES;",
        
        // Create Table (Simple)
        "CREATE TABLE users (\n" +
        "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
        "  name TEXT NOT NULL,\n" +
        "  email TEXT UNIQUE,\n" +
        "  age INTEGER\n" +
        ");",
        
        // Create Table (All Constraints)
        "CREATE TABLE orders (\n" +
        "  order_id INTEGER PRIMARY KEY AUTO_INCREMENT,\n" +
        "  user_id INTEGER,\n" +
        "  amount DECIMAL(10,2) NOT NULL,\n" +
        "  status TEXT DEFAULT 'pending',\n" +
        "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
        "  CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),\n" +
        "  CONSTRAINT check_amount CHECK (amount > 0)\n" +
        ");",
        
        // Show Columns (Describe Table)
        "SHOW COLUMNS FROM users;",
        
        // Insert Data
        "INSERT INTO users (name, email, age)\n" +
        "VALUES ('John Doe', 'john@example.com', 25);",
        
        // Select All
        "SELECT * FROM users;",
        
        // Select With Where
        "SELECT name, age FROM users\n" +
        "WHERE age > 21\n" +
        "ORDER BY name;",
        
        // Update Data
        "UPDATE users\n" +
        "SET age = 26\n" +
        "WHERE name = 'John Doe';",
        
        // Delete Data
        "DELETE FROM users\n" +
        "WHERE age < 18;",
        
        // Drop Table
        "DROP TABLE users;",
        
        // Create Index (for O(log N) performance)
        "CREATE INDEX idx_user_email\n" +
        "ON users(email);",
        
        // Count Rows
        "SELECT COUNT(*) as total FROM users;",

        // --- NEW KEYWORDS ---
        // Alter Table Add
        "ALTER TABLE users ADD COLUMN phone TEXT;",

        // Alter Table Rename
        "ALTER TABLE users RENAME TO customers;",

        // Alter Table Rename Column (SQLite specific)
        "ALTER TABLE users RENAME COLUMN email TO contact_email;",

        // Drop
        "DROP TABLE IF EXISTS users;",

        // Truncate (Simulated in SQLite)
        "DELETE FROM users; VACUUM;",

        // Select DISTINCT
        "SELECT DISTINCT status FROM orders;",

        // Like / Wildcards
        "SELECT * FROM users WHERE name LIKE 'J%';",

        // Group By
        "SELECT status, COUNT(*) FROM orders GROUP BY status;",

        // Having
        "SELECT status, COUNT(*) FROM orders \n" +
        "GROUP BY status \n" +
        "HAVING COUNT(*) > 5;",

        // Limit / Offset
        "SELECT * FROM users LIMIT 10 OFFSET 5;",

        // Join (Inner)
        "SELECT u.name, o.amount \n" +
        "FROM users u \n" +
        "JOIN orders o ON u.id = o.user_id;",

        // Left Join
        "SELECT u.name, o.amount \n" +
        "FROM users u \n" +
        "LEFT JOIN orders o ON u.id = o.user_id;",

        // Union
        "SELECT name FROM users \n" +
        "UNION \n" +
        "SELECT name FROM customers;",
        
        // Transaction
        "BEGIN TRANSACTION;\n" +
        "UPDATE accounts SET balance = balance - 100 WHERE id = 1;\n" +
        "UPDATE accounts SET balance = balance + 100 WHERE id = 2;\n" +
        "COMMIT;",

        // Views
        "CREATE VIEW orders_summary_view AS\n" +
        "SELECT\n" +
            "o.id AS order_id,\n" +
            "u.name AS customer_name,\n" +
            "o.total_amount,\n" +
            "o.order_status,\n" +
            "o.created_at\n" +
        "FROM orders o\n" +
        "JOIN users u ON o.user_id = u.id;",

        // --- ADVANCED (New) ---
        // Date/Time
        "SELECT date('now');",
        "SELECT datetime('now', 'localtime');",
        "SELECT strftime('%Y-%m-%d', 'now');",
        
        // String Operations
        "SELECT upper(name) FROM users;",
        "SELECT substr(name, 1, 3) FROM users;",
        "SELECT name || ' (' || email || ')' FROM users;",
        
        // Math / Aggregates
        "SELECT AVG(age) FROM users;",
        "SELECT SUM(amount) FROM orders;",
        "SELECT ROUND(amount, 2) FROM orders;",
        "SELECT MIN(age), MAX(age) FROM users;",
        
        // Subquery (Nested Select)
        "SELECT * FROM orders \n" +
        "WHERE amount > (SELECT AVG(amount) FROM orders);",
        
        // CASE Statement (Conditional Logic)
        "SELECT name, \n" +
        "CASE \n" +
        "  WHEN age < 18 THEN 'Minor'\n" +
        "  WHEN age >= 18 THEN 'Adult'\n" +
        "  ELSE 'Unknown'\n" +
        "END as status \n" +
        "FROM users;",

        // Show Views
        "SELECT name FROM sqlite_master WHERE type='view';",

        // Show Create View (Definition)
        "SELECT sql FROM sqlite_master \n" +
        "WHERE type='view' AND name='rich_users';",

        // Use View (Select)
        "SELECT * FROM rich_users;",

        // Alter Table Constraint
        "ALTER TABLE products ADD CONSTRAINT check_stock CHECK (stock >= 0);",
        "ALTER TABLE products DROP CONSTRAINT check_stock;",

        // Spatial Index (R-Tree)
        "CREATE VIRTUAL TABLE spatial_index USING rtree(\n" +
        "  id,\n" +
        "  minX, maxX,\n" +
        "  minY, maxY\n" +
        ");"
    };

    /**
     * Gets a template by index.
     * Complexity: O(1)
     */
    public static String getTemplate(int index) {
        if (index >= 0 && index < TEMPLATES.length) {
            return TEMPLATES[index];
        }
        return "";
    }

    /**
     * Gets all template names.
     * Complexity: O(1)
     */
    public static String[] getTemplateNames() {
        return TEMPLATE_NAMES;
    }

    /**
     * Gets performance tips for O(log N) optimization.
     */
    public static String getPerformanceTips() {
        return "PERFORMANCE TIPS:\n" +
               "1. Create indexes on frequently queried columns\n" +
               "2. Use PRIMARY KEY for unique identifiers\n" +
               "3. Use WHERE with indexed columns for O(log N) search\n" +
               "4. Avoid SELECT * on large tables\n" +
               "5. Use EXPLAIN QUERY PLAN to analyze query performance\n\n" +
               "Example:\n" +
               "EXPLAIN QUERY PLAN SELECT * FROM users WHERE id = 1;";
    }

    // --- Autocomplete Keywords ---
    public static final String[] KEYWORDS = {
        "SELECT", "FROM", "WHERE", "TABLE", "UPDATE", "DELETE", "INSERT", "INTO", "VALUES",
        "CREATE", "DROP", "ALTER", "MODIFY", "RENAME", "ADD", "COLUMN", "SET",
        "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "AND", "OR", "CASE", "WHEN",
        "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "ON", "UNION",
        "VIEW", "INDEX", "PRIMARY", "KEY", "FOREIGN", "REFERENCES",
        "DEFAULT", "NULL", "NOT", "UNIQUE", "CHECK", "CONSTRAINT", "AUTO_INCREMENT",
        "DATABASE", "SHOW", "USE", "DESC", "EXPLAIN", "PRAGMA",
        "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "VACUUM", "AS", "IN", "OUT",
        "VARCHAR", "TEXT", "INT", "INTEGER", "REAL", "BLOB", "BOOLEAN", "DATE", "TIME", "DATETIME", "TIMESTAMP",
        "TINYINT", "SMALLINT", "MEDIUMINT", "BIGINT", "UNSIGNED", "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "YEAR",
        "CHAR", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT", "BINARY", "VARBINARY", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB",
        "BOOL", "ENUM", "SET", "JSON", "BIT", "SERIAL", "UUID",
        "SPATIAL", "RTREE", "GEOMETRY", "POINT", "POLYGON"

    };

    public static String[] getKeywords() {
        return KEYWORDS;
    }
}
