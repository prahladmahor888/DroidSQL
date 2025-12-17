package com.smartqueue.droidsql.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartqueue.droidsql.model.DatabaseManager;
import com.smartqueue.droidsql.model.QueryResult;
import com.smartqueue.droidsql.model.SQLCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing database state and SQL execution.
 * Follows MVVM pattern with LiveData for reactive UI updates.
 * 
 * Command History Performance:
 * - ArrayList provides O(1) append and O(1) random access by index
 * - History navigation is O(1) using currentHistoryIndex
 */
public class DatabaseViewModel extends AndroidViewModel {
    private DatabaseManager databaseManager;
    
    // LiveData for reactive UI updates
    private MutableLiveData<QueryResult> executionResult;
    private MutableLiveData<String> terminalOutput;
    private MutableLiveData<Boolean> isDatabaseOpen;
    private MutableLiveData<String> currentDatabaseName;
    
    // Command history with O(1) indexed access
    private List<SQLCommand> commandHistory;
    private int currentHistoryIndex;
    private StringBuilder terminalLog;



    // Thread pool for background database operations (O(1) execution overhead on UI thread)
    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();

    public DatabaseViewModel(@NonNull Application application) {
        super(application);
        databaseManager = new DatabaseManager(application.getApplicationContext());
        
        executionResult = new MutableLiveData<>();
        terminalOutput = new MutableLiveData<>();
        isDatabaseOpen = new MutableLiveData<>(false);
        currentDatabaseName = new MutableLiveData<>("");
        
        commandHistory = new ArrayList<>();
        currentHistoryIndex = -1;
        terminalLog = new StringBuilder();
        
        // Welcome message (MySQL-style)
        appendToTerminal("Welcome to PocketSQL (MySQL Mode)\n");
        appendToTerminal("Type 'help;' for help. Commands end with ; or \\n\n");
    }

    /**
     * Creates or opens a database.
     * Complexity: O(1)
     */
    public void createOrOpenDatabase(String dbName) {
        executor.execute(() -> {
            if (dbName == null || dbName.trim().isEmpty()) {
                postToTerminal("[ERROR] Database name cannot be empty\n");
                return;
            }

            boolean success = databaseManager.openOrCreateDatabase(dbName);
            if (success) {
                isDatabaseOpen.postValue(true);
                currentDatabaseName.postValue(dbName);
                postToTerminal("[SUCCESS] Database '" + dbName + "' is open\n");
                postToTerminal("Ready to execute SQL commands\n\n");
            } else {
                isDatabaseOpen.postValue(false);
                postToTerminal("[ERROR] Failed to open database '" + dbName + "'\n");
            }
        });
    }

    /**
     * Executes a SQL command.
     * Use background thread to prevent UI blocking (ANR).
     * Complexity: O(1) on main thread (async dispatch)
     */
    public void executeSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }

        // Echo command immediately
        appendToTerminal("mysql> " + sql + "\n");
        
        // Add to history
        SQLCommand command = new SQLCommand(sql, System.currentTimeMillis(), true); // Tentative success
        commandHistory.add(command);
        currentHistoryIndex = commandHistory.size();

        executor.execute(() -> {
            // Check for HISTORY command
            if (sql.trim().equalsIgnoreCase("HISTORY") || sql.trim().equalsIgnoreCase("HISTORY;")) {
                 showHistory();
                 return;
            }

            // Execute in background
            QueryResult result = databaseManager.executeSQL(sql);
            
            // ... (rest of execution logic)

            
            // Update history status
            command.setSuccess(result.isSuccess());
            
            // Update LiveData (postValue determines main thread dispatch)
            executionResult.postValue(result);

            // Format output MySQL-style
            if (result.isSuccess()) {
                StringBuilder output = new StringBuilder();
                
                // Check if it's a USE command
                if (sql.trim().toUpperCase().startsWith("USE ")) {
                    output.append("Database changed\n");
                    
                    // CRITICAL FIX: Update LiveData so UI Status Bar updates
                    String newDbName = databaseManager.getCurrentDatabaseName();
                    currentDatabaseName.postValue(newDbName);
                }
                // Check if it has table results
                else if (result.hasRows() && result.hasColumns()) {
                    output.append(formatAsAsciiTable(result));
                    output.append(result.getRowsAffected() + " row" + 
                        (result.getRowsAffected() == 1 ? "" : "s") + " in set");
                    if (result.getExecutionTimeMs() > 0) {
                        double seconds = result.getExecutionTimeMs() / 1000.0;
                        output.append(String.format(" (%.2f sec)", seconds));
                    }
                    output.append("\n");
                }
                // Regular success message
                else {
                    output.append(result.getMessage() + "\n");
                }
                output.append("\n");
                postToTerminal(output.toString());
            } else {
                // Return EXIT signal if needed
                if (result.shouldExitApp()) {
                     // Nothing to log, main thread observer handles exit
                     return;
                }
                
                // Format error MySQL-style
                String errorMsg = result.getMessage().replace("ERROR: ", "");
                int errorCode = 1064; 
                String sqlState = "42000";
                
                if (errorMsg.toLowerCase().contains("no such table") || 
                    errorMsg.toLowerCase().contains("doesn't exist")) {
                    errorCode = 1146; sqlState = "42S02";
                } else if (errorMsg.toLowerCase().contains("no database")) {
                    errorCode = 1049; sqlState = "42000";
                } else if (errorMsg.toLowerCase().contains("syntax error")) {
                    errorCode = 1064; sqlState = "42000";
                } else if (errorMsg.toLowerCase().contains("already exists")) {
                    errorCode = 1050; sqlState = "42S01";
                }
                
                postToTerminal(String.format("ERROR %d (%s): %s\n\n", errorCode, sqlState, errorMsg));
            }
        });
    }

    /**
     * Formats query result as ASCII table (MySQL-style).
     * Includes LIMIT to ensure O(1) rendering time regardless of DB size.
     */
    private String formatAsAsciiTable(QueryResult result) {
        if (!result.hasColumns()) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        List<String> columns = result.getColumnNames();
        List<List<String>> rows = result.getRows();
        
        // LIMIT OUTPUT to prevent UI freezing (O(N) -> O(1) perceived)
        int MAX_DISPLAY_ROWS = 100;
        boolean truncated = rows.size() > MAX_DISPLAY_ROWS;
        List<List<String>> displayRows = truncated ? rows.subList(0, MAX_DISPLAY_ROWS) : rows;

        // Calculate column widths
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).length();
        }
        for (List<String> row : displayRows) {
            for (int i = 0; i < row.size(); i++) {
                String value = row.get(i);
                if (value != null && value.length() > widths[i]) {
                    widths[i] = value.length();
                }
            }
        }

        // Build top border
        output.append(buildBorder(widths));

        // Build header row
        output.append("|");
        for (int i = 0; i < columns.size(); i++) {
            output.append(" ");
            output.append(padRight(columns.get(i), widths[i]));
            output.append(" |");
        }
        output.append("\n");

        // Build middle border
        output.append(buildBorder(widths));

        // Build data rows
        for (List<String> row : displayRows) {
            output.append("|");
            for (int i = 0; i < row.size(); i++) {
                output.append(" ");
                String value = row.get(i);
                if (value == null) value = "NULL";
                output.append(padRight(value, widths[i]));
                output.append(" |");
            }
            output.append("\n");
        }

        // Build bottom border
        output.append(buildBorder(widths));
        
        if (truncated) {
            output.append("... ").append(rows.size() - MAX_DISPLAY_ROWS).append(" more rows hidden for performance ...\n\n");
        }

        return output.toString();
    }

    /**
     * Builds a border line for ASCII table.
     */
    private String buildBorder(int[] widths) {
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) {
            for (int i = 0; i < width + 2; i++) {
                border.append("-");
            }
            border.append("+");
        }
        border.append("\n");
        return border.toString();
    }

    /**
     * Pads string to right with spaces.
     */
    private String padRight(String s, int width) {
        if (s.length() >= width) {
            return s;
        }
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() < width) {
            padded.append(" ");
        }
        return padded.toString();
    }

    /**
     * Navigates through command history.
     * Direction: -1 for previous, +1 for next
     * Complexity: O(1) - direct array access by index
     */
    public String navigateHistory(int direction) {
        if (commandHistory.isEmpty()) {
            return "";
        }

        // Calculate new index
        int newIndex = currentHistoryIndex + direction;

        // Boundary checks
        if (newIndex < 0) {
            newIndex = 0;
        } else if (newIndex >= commandHistory.size()) {
            newIndex = commandHistory.size();
            currentHistoryIndex = newIndex;
            return ""; // Beyond end, return empty
        }

        currentHistoryIndex = newIndex;
        
        // O(1) access by index
        if (currentHistoryIndex < commandHistory.size()) {
            return commandHistory.get(currentHistoryIndex).getCommandText();
        }
        return "";
    }

    /**
     * Exports current database to Downloads.
     * Complexity: O(N) where N = database file size
     */
    public void exportCurrentDatabase() {
        boolean success = databaseManager.exportDatabase();
        if (success) {
            appendToTerminal("[SUCCESS] Database exported to Downloads folder\n\n");
        } else {
            appendToTerminal("[ERROR] Failed to export database\n\n");
        }
    }

    /**
     * Gets list of tables in current database.
     * Complexity: O(T) where T = number of tables
     */
    /**
     * Gets list of tables in current database.
     * Async operation.
     * Complexity: O(T) where T = number of tables
     */
    public void listTables() {
        executor.execute(() -> {
            List<String> tables = databaseManager.getTableNames();
            if (tables.isEmpty()) {
                postToTerminal("[INFO] No tables found in database\n\n");
            } else {
                postToTerminal("[INFO] Tables in database:\n");
                for (String table : tables) {
                    postToTerminal("  - " + table + "\n");
                }
                postToTerminal("\n");
            }
        });
    }

    /**
     * Clears the terminal output.
     * Complexity: O(1)
     */
    public void clearTerminal() {
        terminalLog = new StringBuilder();
        terminalLog.append("Terminal cleared\n\nmysql> ");
        terminalOutput.setValue(terminalLog.toString());
    }

    /**
     * Appends text to terminal log (Main Thread).
     */
    private void appendToTerminal(String text) {
        terminalLog.append(text);
        terminalOutput.setValue(terminalLog.toString());
    }

    /**
     * Appends text to terminal log (Background Thread).
     * Synchronized to prevent race conditions on StringBuilder.
     */
    private synchronized void postToTerminal(String text) {
        terminalLog.append(text);
        terminalOutput.postValue(terminalLog.toString());
    }

    // LiveData Getters for UI observation
    public LiveData<QueryResult> getExecutionResult() {
        return executionResult;
    }

    public LiveData<String> getTerminalOutput() {
        return terminalOutput;
    }

    public LiveData<Boolean> getIsDatabaseOpen() {
        return isDatabaseOpen;
    }

    public LiveData<String> getCurrentDatabaseName() {
        return currentDatabaseName;
    }

    public List<SQLCommand> getCommandHistory() {
        return commandHistory;
    }

    /**
     * Generates a sample E-commerce database.
     * Async operation.
     */
    public void generateSampleDatabase() {
        executor.execute(() -> {
            String dbName = "ecommerce.db";
            
            // 1. Create/Open Database
            boolean openSuccess = databaseManager.openOrCreateDatabase(dbName);
            if (!openSuccess) {
                postToTerminal("[ERROR] Failed to create sample database\n");
                return;
            }
            
            // Notify UI of open
            isDatabaseOpen.postValue(true);
            currentDatabaseName.postValue(dbName);
            postToTerminal("[SUCCESS] Created database '" + dbName + "'\n");
            postToTerminal("Generating sample data (Tables: users, products, orders...)\n");

            // 2. Get SQL commands
            java.util.List<String> commands = com.smartqueue.droidsql.utils.SampleDatabaseGenerator.getEcommerceSQL();
            
            // 3. Execute commands
            int successCount = 0;
            for (String sql : commands) {
                QueryResult result = databaseManager.executeSQL(sql);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    postToTerminal("[WARNING] Failed: " + sql + " -> " + result.getMessage() + "\n");
                }
            }
            
            postToTerminal("[SUCCESS] Sample database ready! (" + successCount + " commands executed)\n");
            postToTerminal("Try: SHOW TABLES; or SELECT * FROM products;\n\n");
        });
    }

    /**
     * Shows command history in terminal.
     * Uses getCommandHistory() as requested.
     */
    public void showHistory() {
        // We are already in background thread if called from executeSQL
        // But to be safe for UI calls, we wrap or check
        // For simplicity, just postToTerminal since list access is fast enough or synchronized
        
        List<SQLCommand> history = getCommandHistory();
        if (history.isEmpty()) {
            postToTerminal("[INFO] History is empty\n\n");
        } else {
            postToTerminal("[INFO] Command History:\n");
            for (int i = 0; i < history.size(); i++) {
                SQLCommand cmd = history.get(i);
                postToTerminal(String.format("%d. %s [%s]\n", 
                    i + 1, 
                    cmd.getCommandText(),
                    cmd.wasSuccessful() ? "OK" : "FAIL"));
            }
            postToTerminal("\n");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        databaseManager.closeDatabase();
    }
}
