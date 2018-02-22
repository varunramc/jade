package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
public enum ExecutionStatus {
    PENDING,
    DISPATCHING,
    RUNNING,
    AWAITING_CHILD_COMPLETION,
    SUCCEEDED,
    FAILED,
    TIMED_OUT;

    public boolean isComplete() {
        return this == ExecutionStatus.FAILED || this == ExecutionStatus.SUCCEEDED || this == ExecutionStatus.TIMED_OUT;
    }
}
