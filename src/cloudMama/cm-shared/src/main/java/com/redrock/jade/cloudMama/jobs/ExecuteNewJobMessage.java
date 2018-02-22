package com.redrock.jade.cloudMama.jobs;

import java.io.Serializable;

/**
 * Copyright RedRock 2013-14
 */
final class ExecuteNewJobMessage extends ExecuteJobMessage {

    public ExecuteNewJobMessage(JobExecutionContext jobExecutionContext) {
        super(jobExecutionContext);
    }
}
