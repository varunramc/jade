package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
final class ResumeJobOnChildrenCompletionMessage extends JobMessage {
    private final Iterable<Job> completedChildren;

    public ResumeJobOnChildrenCompletionMessage(Job job, Iterable<Job> completedChildren) {
        super(job);
        this.completedChildren = completedChildren;
    }

    public ResumeJobOnChildrenCompletionMessage() {
        super(null);
        completedChildren = null;
    }

    public Iterable<Job> getCompletedChildren() {
        return completedChildren;
    }
}
