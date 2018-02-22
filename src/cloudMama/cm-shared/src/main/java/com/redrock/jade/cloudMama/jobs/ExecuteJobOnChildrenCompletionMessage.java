package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
final class ExecuteJobOnChildrenCompletionMessage extends ExecuteJobMessage {
    private final Iterable<Job> children;

    public ExecuteJobOnChildrenCompletionMessage(JobExecutionContext jobExecutionContext, Iterable<Job> children) {
        super(jobExecutionContext);

        this.children = children;
    }

    public Iterable<Job> getChildren() {
        return children;
    }
}
