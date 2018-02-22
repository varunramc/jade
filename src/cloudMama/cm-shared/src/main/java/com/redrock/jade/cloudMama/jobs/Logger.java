package com.redrock.jade.cloudMama.jobs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright RedRock 2013-14
 */
public final class Logger {

    private List<LogEntry> logEntries;

    Logger() {
        logEntries = new LinkedList<>();
    }

    public void logError(String message) {
        logMessage(LogLevel.ERROR, message);
    }

    public void logWarning(String message) {
        logMessage(LogLevel.WARNING, message);
    }

    public void logInfo(String message) {
        logMessage(LogLevel.INFO, message);
    }

    public void logVerbose(String message) {
        logMessage(LogLevel.VERBOSE, message);
    }

    private synchronized void logMessage(LogLevel level, String message) {
        logEntries.add(new LogEntry(level, message));
    }

    synchronized Collection<LogEntry> resetAndGetLogEntries() {
        Collection<LogEntry> currentEntries = logEntries;
        logEntries = new LinkedList<>();

        return currentEntries;
    }

    Collection<LogEntry> getLogEntries() {
        return logEntries;
    }
}
