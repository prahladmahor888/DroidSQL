package com.smartqueue.droidsql.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing the result of a SQL query or command execution.
 * Designed to be schema-agnostic - works with any table structure.
 */
public class QueryResult {
    private boolean isSuccess;
    private String message;
    private List<String> columnNames;
    private List<List<String>> rows;
    private int rowsAffected;
    private long executionTimeMs;

    public QueryResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.rowsAffected = 0;
        this.executionTimeMs = 0;
    }

    // Constructor for query results with data
    public QueryResult(boolean isSuccess, String message, List<String> columnNames, List<List<String>> rows) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.columnNames = columnNames;
        this.rows = rows;
        this.rowsAffected = rows.size();
        this.executionTimeMs = 0;
    }

    // Getters
    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    // Setters
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    // Check if this result contains data rows
    public boolean hasRows() {
        return rows != null && !rows.isEmpty();
    }

    // Check if this result has columns
    public boolean hasColumns() {
        return columnNames != null && !columnNames.isEmpty();
    }

    // Flag to indicate if the app should exit
    private boolean shouldExitApp = false;

    public boolean shouldExitApp() {
        return shouldExitApp;
    }

    public void setShouldExitApp(boolean shouldExitApp) {
        this.shouldExitApp = shouldExitApp;
    }
}
