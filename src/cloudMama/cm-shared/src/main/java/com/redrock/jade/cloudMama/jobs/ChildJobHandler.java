package com.redrock.jade.cloudMama.jobs;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Copyright RedRock 2013-14
 */
class ChildJobHandler implements Serializable {
    private final String jobId;
    private final MethodSignature onCompleteHandlerSignature;

    /**
     * Empty constructor for Jackson
     */
    private ChildJobHandler() {
        jobId = null;
        onCompleteHandlerSignature = null;
    }

    public ChildJobHandler(String jobId, MethodSignature onCompleteHandlerSignature) {
        Preconditions.checkNotNull(jobId);

        this.jobId = jobId;
        this.onCompleteHandlerSignature = onCompleteHandlerSignature;
    }

    public void onComplete(Job completedJob, Job handlerJob) throws Exception {
        Preconditions.checkNotNull(completedJob);
        Preconditions.checkState(completedJob.getId().equals(jobId));

        invokeCompletionHandlerIfSet(completedJob, handlerJob);
    }

    private void invokeCompletionHandlerIfSet(Job completedJob, Job handlerJob) throws Exception {
        if (onCompleteHandlerSignature != null) {
            Method method = onCompleteHandlerSignature.getMethod(handlerJob.getClass());
            method.invoke(handlerJob, completedJob);
        }
    }
}
