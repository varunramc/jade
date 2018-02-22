package com.redrock.jade.cloudMama.jobs;

import java.io.Serializable;

/**
 * Copyright RedRock 2013-14
 */
public final class LogEntry implements Serializable {
    private final LogLevel level;
    private final String message;

    LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }
}
