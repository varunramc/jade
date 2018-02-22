package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.jobs.JobLogEntry;
import com.redrock.jade.cloudMama.jobs.LogLevel;

/**
 * Copyright RedRock 2013-14
 */
public final class JobLogPayload {
    private final String   id;
    private final long     timestamp;
    private final String   jobId;
    private final LogLevel level;
    private final String   message;

    public JobLogPayload(JobLogEntry logEntry) {
        id = logEntry.getId();
        timestamp = logEntry.getTimestamp();
        jobId = logEntry.getJob().getId();
        level = logEntry.getLevel();
        message = logEntry.getMessage();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getJobId() {
        return jobId;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
