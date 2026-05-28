package com.smartqueue.droidsql;

import org.junit.Test;
import java.util.List;
import java.lang.reflect.Method;
import com.smartqueue.droidsql.model.DatabaseManager;

import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSanitizeQuery_startTransaction() throws Exception {
        DatabaseManager dbManager = new DatabaseManager(null);
        Method sanitizeMethod = DatabaseManager.class.getDeclaredMethod("sanitizeQuery", String.class);
        sanitizeMethod.setAccessible(true);

        // Test basic START TRANSACTION
        String sql1 = "START TRANSACTION;";
        String result1 = (String) sanitizeMethod.invoke(dbManager, sql1);
        assertEquals("BEGIN TRANSACTION;", result1);

        // Test case insensitivity and extra whitespaces
        String sql2 = "  start   transaction  ; ";
        String result2 = (String) sanitizeMethod.invoke(dbManager, sql2);
        assertEquals("  BEGIN TRANSACTION  ; ", result2);

        // Test with characteristics: READ WRITE
        String sql3 = "START TRANSACTION READ WRITE;";
        String result3 = (String) sanitizeMethod.invoke(dbManager, sql3);
        assertEquals("BEGIN TRANSACTION;", result3);

        // Test with characteristics: READ ONLY
        String sql4 = "START TRANSACTION READ ONLY;";
        String result4 = (String) sanitizeMethod.invoke(dbManager, sql4);
        assertEquals("BEGIN TRANSACTION;", result4);

        // Test with characteristics: WITH CONSISTENT SNAPSHOT
        String sql5 = "START TRANSACTION WITH CONSISTENT SNAPSHOT;";
        String result5 = (String) sanitizeMethod.invoke(dbManager, sql5);
        assertEquals("BEGIN TRANSACTION;", result5);

        // Test table name that contains TRANSACTION (should NOT match)
        String sql6 = "SELECT * FROM START_TRANSACTION;";
        String result6 = (String) sanitizeMethod.invoke(dbManager, sql6);
        assertEquals("SELECT * FROM START_TRANSACTION;", result6);

        // Test START TRANSACTION in double quotes (should NOT match)
        String sql7 = "INSERT INTO logs (message) VALUES (\"START TRANSACTION\");";
        String result7 = (String) sanitizeMethod.invoke(dbManager, sql7);
        assertEquals("INSERT INTO logs (message) VALUES (\"START TRANSACTION\");", result7);
    }

    @Test
    public void testSanitizeQuery_autoIncrement() throws Exception {
        DatabaseManager dbManager = new DatabaseManager(null);
        Method sanitizeMethod = DatabaseManager.class.getDeclaredMethod("sanitizeQuery", String.class);
        sanitizeMethod.setAccessible(true);

        // Test INT PRIMARY KEY AUTO_INCREMENT -> INTEGER PRIMARY KEY AUTOINCREMENT
        String sql1 = "id INT PRIMARY KEY AUTO_INCREMENT";
        String result1 = (String) sanitizeMethod.invoke(dbManager, sql1);
        assertEquals("id INTEGER PRIMARY KEY AUTOINCREMENT", result1);

        // Test INT AUTO_INCREMENT PRIMARY KEY -> INTEGER PRIMARY KEY AUTOINCREMENT
        String sql2 = "id INT AUTO_INCREMENT PRIMARY KEY";
        String result2 = (String) sanitizeMethod.invoke(dbManager, sql2);
        assertEquals("id INTEGER PRIMARY KEY AUTOINCREMENT", result2);

        // Test with case insensitivity and extra spaces
        String sql3 = "id   int   primary   key   auto_increment";
        String result3 = (String) sanitizeMethod.invoke(dbManager, sql3);
        assertEquals("id   INTEGER PRIMARY KEY AUTOINCREMENT", result3);

        // Test table containing students creation schema
        String sql4 = "CREATE TABLE students (\n" +
                "    id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    name VARCHAR(100)\n" +
                ");";
        String result4 = (String) sanitizeMethod.invoke(dbManager, sql4);
        assertTrue(result4.contains("id INTEGER PRIMARY KEY AUTOINCREMENT"));
    }

    @Test
    public void testOpenOrCreateDatabase_traversalDefense() {
        DatabaseManager dbManager = new DatabaseManager(null);
        
        // Should return false for traversal attempts
        assertFalse(dbManager.openOrCreateDatabase("../malicious"));
        assertFalse(dbManager.openOrCreateDatabase("subfolder/dbName"));
        assertFalse(dbManager.openOrCreateDatabase("folder\\dbName"));
        assertFalse(dbManager.openOrCreateDatabase(null));
    }

    @Test
    public void testSQLImportHelper_isValidSQLiteHeader() throws Exception {
        byte[] valid = "SQLite format 3\0".getBytes();
        assertTrue(com.smartqueue.droidsql.utils.SQLImportHelper.isValidSQLiteHeader(new java.io.ByteArrayInputStream(valid)));

        byte[] invalid = "SQL-NOT-VALID!!".getBytes();
        assertFalse(com.smartqueue.droidsql.utils.SQLImportHelper.isValidSQLiteHeader(new java.io.ByteArrayInputStream(invalid)));
    }

    @Test
    public void testSQLImportHelper_parseCSV() throws Exception {
        String csv = "id,name,age\n1,Alice,30\n2,\"Bob, Jr.\",25\n";
        List<List<String>> result = com.smartqueue.droidsql.utils.SQLImportHelper.parseCSV(
            new java.io.ByteArrayInputStream(csv.getBytes())
        );

        assertEquals(3, result.size());
        assertEquals("id", result.get(0).get(0));
        assertEquals("name", result.get(0).get(1));
        assertEquals("age", result.get(0).get(2));

        assertEquals("1", result.get(1).get(0));
        assertEquals("Alice", result.get(1).get(1));
        assertEquals("30", result.get(1).get(2));

        assertEquals("2", result.get(2).get(0));
        assertEquals("Bob, Jr.", result.get(2).get(1));
        assertEquals("25", result.get(2).get(2));
    }

    @Test
    public void testStripComments() {
        // Test basic line comments --
        String sql1 = "-- comment here\nSELECT * FROM t;";
        assertEquals("SELECT * FROM t;", DatabaseManager.stripComments(sql1).trim());

        // Test line comments #
        String sql2 = "# comment here\nSELECT * FROM t;";
        assertEquals("SELECT * FROM t;", DatabaseManager.stripComments(sql2).trim());

        // Test block comments /* */
        String sql3 = "/* block comment */ SELECT * FROM t;";
        assertEquals("SELECT * FROM t;", DatabaseManager.stripComments(sql3).trim());

        // Test comment symbols inside string literals (should NOT be stripped)
        String sql4 = "INSERT INTO t (val) VALUES ('-- not a comment');";
        assertEquals("INSERT INTO t (val) VALUES ('-- not a comment');", DatabaseManager.stripComments(sql4).trim());

        // Test comment symbols inside double quotes (should NOT be stripped)
        String sql5 = "INSERT INTO t (val) VALUES (\"/* not a comment */\");";
        assertEquals("INSERT INTO t (val) VALUES (\"/* not a comment */\");", DatabaseManager.stripComments(sql5).trim());

        // Test comments at the beginning of custom commands
        String sql6 = "-- Create Database\nCREATE DATABASE student_management;";
        assertEquals("CREATE DATABASE student_management;", DatabaseManager.stripComments(sql6).trim());
    }
}