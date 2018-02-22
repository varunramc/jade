package com.redrock.jade.cloudMama.jobs;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Copyright RedRock 2013-14
 */
final class JobProgressMessage extends JobMessage {
    private final List<LogEntry> logEntries;
    private final List<Job> childJobsToStart;

    public JobProgressMessage(JobExecutionContext executionContext) {
        super(executionContext.getJob());

        Collection<LogEntry> logEntryCollection =
                executionContext.getEnvironment().log().resetAndGetLogEntries();
        logEntries = new ArrayList<>(logEntryCollection);
        childJobsToStart = getJob().resetAndGetChildJobsToStart();
    }

    public JobProgressMessage() {
        super(null);

        logEntries = null;
        childJobsToStart = null;
    }

    public Iterable<LogEntry> getLogEntries() {
        return logEntries;
    }

    public List<Job> getChildJobsToStart() {
        return childJobsToStart;
    }
}
