package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.shared.dao.CollectionName;
import com.redrock.jade.shared.dao.Document;
import com.redrock.jade.shared.dao.DocumentReference;

/**
 * Copyright RedRock 2013-14
 */
@CollectionName("jobLogs")
public final class JobLogEntry extends Document {
    private final DocumentReference<Job> job;
    private final long                   timestamp;
    private final LogLevel               level;
    private final String                 message;

    JobLogEntry() {
        job = null;
        timestamp = 0;
        level = LogLevel.VERBOSE;
        message = null;
    }

    JobLogEntry(Job job, LogLevel level, String message) {
        this.job = new DocumentReference<>(job);
        timestamp = System.currentTimeMillis();
        this.level = level;
        this.message = message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public DocumentReference<Job> getJob() {
        return job;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
