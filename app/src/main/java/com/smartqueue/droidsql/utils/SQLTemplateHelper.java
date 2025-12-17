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
        "Count Rows"
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
        "SELECT COUNT(*) as total FROM users;"
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
}
