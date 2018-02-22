package com.redrock.jade.cloudMama.jobs;

import java.io.Serializable;

/**
 * Copyright RedRock 2013-14
 */
abstract class ExecuteJobMessage implements Serializable {
    private final JobExecutionContext jobExecutionContext;

    public ExecuteJobMessage(JobExecutionContext jobExecutionContext) {
        this.jobExecutionContext = jobExecutionContext;
    }

    public JobExecutionContext getJobExecutionContext() {
        return jobExecutionContext;
    }
}
