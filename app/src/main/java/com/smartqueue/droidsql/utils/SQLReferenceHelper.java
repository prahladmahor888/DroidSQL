package com.smartqueue.droidsql.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to store and retrieve SQL syntax reference data.
 * Contains categorized lists of SQL clauses, operators, and functions.
 */
public class SQLReferenceHelper {

    private static final Map<String, List<String[]>> CATEGORIES = new LinkedHashMap<>();

    // OPTIMIZATION: TreeMap for O(log N) lookups and prefix searching (Red-Black Tree)
    private static final java.util.TreeMap<String, String> COMMAND_INDEX = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        // 1. Filtering / Conditions
        addCategory("Filtering", new String[][]{
            {"WHERE", "Filter rows before grouping"},
            {"HAVING", "Filter groups after GROUP BY"},
            {"IN", "Match values in a list"},
            {"BETWEEN", "Range filtering"},
            {"LIKE", "Pattern matching"},
            {"IS NULL", "Check for NULL values"},
            {"EXISTS", "Check subquery existence"}
        });

        // 2. Logical Operators
        addCategory("Logical", new String[][]{
            {"AND", "Both conditions must be true"},
            {"OR", "At least one condition must be true"},
            {"NOT", "Negates a condition"},
            {"XOR", "Logical XOR (one true, not both)"}
        });

        // 3. Grouping & Aggregation
        addCategory("Grouping", new String[][]{
            {"GROUP BY", "Group rows sharing a property"},
            {"COUNT()", "Count rows"},
            {"SUM()", "Sum values"},
            {"AVG()", "Average value"},
            {"MIN()", "Minimum value"},
            {"MAX()", "Maximum value"}
        });

        // 4. Sorting & Limiting
        addCategory("Sorting", new String[][]{
            {"ORDER BY", "Sort result set"},
            {"ASC/DESC", "Ascending or Descending"},
            {"LIMIT", "Limit number of rows"},
            {"OFFSET", "Skip initial rows"}
        });

        // 5. Join Clauses
        addCategory("Joins", new String[][]{
            {"INNER JOIN", "Returns matching rows in both"},
            {"LEFT JOIN", "Returns all from left, matched from right"},
            {"CROSS JOIN", "Cartesian product of tables"},
            {"ON", "Join condition"},
            {"USING", "Join condition (same column name)"}
        });

        // 6. DML (Data Manipulation)
        addCategory("DML", new String[][]{
            {"INSERT INTO", "Insert new rows"},
            {"UPDATE", "Modify existing rows"},
            {"DELETE FROM", "Remove rows"},
            {"VALUES", "Specify values for INSERT"}
        });

        // 7. DDL (Data Definition)
        addCategory("DDL", new String[][]{
            {"CREATE", "Create table/index/view"},
            {"ALTER", "Modify table structure"},
            {"DROP", "Delete table/index/view"},
            {"TRUNCATE", "Delete all rows (SQLite: DELETE)"}
        });

        // 8. Constraints
        addCategory("Constraints", new String[][]{
            {"PRIMARY KEY", "Unique identifier"},
            {"FOREIGN KEY", "Link to another table"},
            {"UNIQUE", "Ensure unique values"},
            {"NOT NULL", "Cannot be empty"},
            {"CHECK", "Ensure value meets condition"},
            {"DEFAULT", "Default value if none provided"}
        });
        
        // 9. TCL (Transaction Control)
        addCategory("Transactions", new String[][]{
            {"START TRANSACTION", "Start output transaction"},
            {"COMMIT", "Save changes"},
            {"ROLLBACK", "Undo changes"},
            {"SAVEPOINT", "Set savepoint"},
            {"SET AUTOCOMMIT", "Toggle auto-commit"}
        });
        
        // 10. CREATE TABLE Extras
        addCategory("Create Extras", new String[][]{
            {"IF NOT EXISTS", "Prevent error if exists"},
            {"ENGINE", "Storage engine (InnoDB/MyISAM)"},
            {"AUTO_INCREMENT", "Set initial ID value"},
            {"DEFAULT CHARSET", "Set default character set"},
            {"COLLATE", "Set collation rules"},
            {"COMMENT", "Add table comment"},
            {"ROW_FORMAT", "Define row storage format"}
        });

        // 11. ALTER TABLE Clauses
        addCategory("Alter Table", new String[][]{
            {"ADD COLUMN", "Add new column"},
            {"DROP COLUMN", "Remove column"},
            {"MODIFY COLUMN", "Change column type"},
            {"CHANGE COLUMN", "Rename & change column"},
            {"RENAME TO", "Rename table"},
            {"ADD INDEX", "Create index"},
            {"ADD FOREIGN KEY", "Create FK constraint"}
        });

        // 12. SELECT Special
        addCategory("Select Special", new String[][]{
            {"SQL_CALC_FOUND_ROWS", "Calc rows without limit"},
            {"FOUND_ROWS()", "Get calculated row count"},
            {"STRAIGHT_JOIN", "Force join order"},
            {"HIGH_PRIORITY", "Execute before updates"},
            {"SQL_NO_CACHE", "Don't cache result"}
        });

        // 13. INSERT/UPDATE/DELETE Special
        addCategory("DML Special", new String[][]{
            {"INSERT IGNORE", "Skip errors on duplicate"},
            {"ON DUPLICATE KEY UPDATE", "Update if exists"},
            {"LOW_PRIORITY", "Delay execution"},
            {"DELAYED", "Queue insert (deprecated)"},
            {"QUICK", "Skip index merge in delete"}
        });

        // 14. Locking & Control
        addCategory("Locking", new String[][]{
            {"LOCK TABLES", "Lock table for r/w"},
            {"UNLOCK TABLES", "Release locks"},
            {"READ / WRITE", "Lock mode"}
        });

        // 15. Views & Procedures
        addCategory("Views/Procs", new String[][]{
            {"CREATE VIEW", "Create virtual table"},
            {"CREATE PROCEDURE", "Create stored proc"},
            {"DELIMITER", "Change statement delimiter"},
            {"DECLARE", "Define variable/cursor"},
            {"LOOP / WHILE", "Flow control"}
        });

        // 16. Database Maintenance
        addCategory("Maintenance", new String[][]{
            {"ANALYZE TABLE", "Analyze key distribution"},
            {"OPTIMIZE TABLE", "Reclaim unused space"},
            {"EXPLAIN", "Show query execution plan"},
            {"FLUSH", "Clear internal caches"}
        });

        // 17. Storage & Partitioning
        addCategory("Partitions", new String[][]{
            {"PARTITION BY", "Split table data"},
            {"HASH", "Hash partitioning"},
            {"RANGE", "Range partitioning"},
            {"LIST", "List partitioning"},
            {"SUBPARTITION", "Sub-level partitioning"}
        });

        // 18. Exit / Quit (Shell Control)
        addCategory("Exit/Shell", new String[][]{
            {"EXIT", "Exit terminal/app"},
            {"QUIT", "Exit terminal/app"},
            {"\\q", "Exit terminal/app"},
            {"Ctrl+D", "Exit (Linux/Mac style)"}
        });

        // 19. Procedure Flow Control
        addCategory("Proc Flow", new String[][]{
            {"LEAVE", "Exit loop"},
            {"ITERATE", "Continue loop"},
            {"RETURN", "Exit function"},
            {"END", "Close block"},
            {"IF / ELSEIF", "Conditional logic"},
            {"CASE / WHEN", "Switch-case logic"}
        });

        // 20. Cursors (Advanced)
        addCategory("Cursors", new String[][]{
            {"DECLARE cursor", "Define cursor"},
            {"OPEN cursor", "Open cursor"},
            {"FETCH cursor", "Get row"},
            {"CLOSE cursor", "Close cursor"}
        });

        // 21. Script / Error Handling
        addCategory("Script/Error", new String[][]{
            {"DELIMITER", "Change delimiter"},
            {"SOURCE", "Run SQL script file"},
            {"DECLARE HANDLER", "Handle exceptions"},
            {"SIGNAL", "Raise error"},
            {"RESIGNAL", "Propagate error"}
        });
    }

    private static void addCategory(String name, String[][] items) {
        List<String[]> list = new ArrayList<>();
        for (String[] item : items) {
            list.add(item);
            // Build O(log N) index
            COMMAND_INDEX.put(item[0], item[1]);
        }
        CATEGORIES.put(name, list);
    }

    public static Map<String, List<String[]>> getAllCategories() {
        return CATEGORIES;
    }

    /**
     * Searches for commands starting with prefix.
     * Uses TreeMap for O(log N) search complexity.
     * efficient than iterating full list O(N).
     * @param prefix Prefix to search for (e.g., "CRE")
     * @return Map of matching commands
     */
    public static Map<String, String> searchCommands(String prefix) {
        if (prefix == null || prefix.isEmpty()) return new LinkedHashMap<>();
        
        // O(log N) lookup to find start of range
        java.util.SortedMap<String, String> tail = COMMAND_INDEX.tailMap(prefix);
        
        Map<String, String> results = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : tail.entrySet()) {
            if (entry.getKey().toUpperCase().startsWith(prefix.toUpperCase())) {
                results.put(entry.getKey(), entry.getValue());
            } else {
                // Optimization: Stop iterating once prefix no longer matches
                // Effectively O(K + log N) where K is number of matches
                break; 
            }
        }
        return results;
    }
}
