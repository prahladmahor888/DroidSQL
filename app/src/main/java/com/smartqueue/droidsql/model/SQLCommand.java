package com.smartqueue.droidsql.model;

/**
 * Represents a SQL command in the command history.
 * Used for efficient command navigation with O(1) access by index.
 */
public class SQLCommand {
    private String commandText;
    private long timestamp;
    private boolean wasSuccessful;

    public SQLCommand(String commandText, long timestamp, boolean wasSuccessful) {
        this.commandText = commandText;
        this.timestamp = timestamp;
        this.wasSuccessful = wasSuccessful;
    }

    // Getters
    public String getCommandText() {
        return commandText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    // Setters
    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    // Alias for setWasSuccessful for compatibility
    public void setSuccess(boolean success) {
        this.wasSuccessful = success;
    }

    @Override
    public String toString() {
        return commandText;
    }
}
