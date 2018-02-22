package com.redrock.jade.cloudMama.jobs;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class LoggerTest {

    @Test
    public void testLogError() throws Exception {
        Logger logger = new Logger();
        logger.logError("message");

        Collection<LogEntry> logEntries = logger.getLogEntries();
        assertTrue(logEntries.size() == 1);
        assertTrue(logEntries.iterator().next().getLevel() == LogLevel.ERROR);
    }

    @Test
    public void testLogWarning() throws Exception {
        Logger logger = new Logger();
        logger.logWarning("message");

        Collection<LogEntry> logEntries = logger.getLogEntries();
        assertTrue(logEntries.size() == 1);
        assertTrue(logEntries.iterator().next().getLevel() == LogLevel.WARNING);
    }

    @Test
    public void testLogInfo() throws Exception {
        Logger logger = new Logger();
        logger.logInfo("message");

        Collection<LogEntry> logEntries = logger.getLogEntries();
        assertTrue(logEntries.size() == 1);
        assertTrue(logEntries.iterator().next().getLevel() == LogLevel.INFO);
    }

    @Test
    public void testLogVerbose() throws Exception {
        Logger logger = new Logger();
        logger.logVerbose("message");

        Collection<LogEntry> logEntries = logger.getLogEntries();
        assertTrue(logEntries.size() == 1);
        assertTrue(logEntries.iterator().next().getLevel() == LogLevel.VERBOSE);
    }

    @Test
    public void testResetAndGetLogEntries() throws Exception {
        Logger logger = new Logger();
        logger.logVerbose("message");
        logger.logError("error");
        assertTrue(logger.getLogEntries().size() == 2);

        Collection<LogEntry> logEntries = logger.resetAndGetLogEntries();
        assertTrue(logEntries.size() == 2);
        assertTrue(logger.getLogEntries().isEmpty());
    }
}