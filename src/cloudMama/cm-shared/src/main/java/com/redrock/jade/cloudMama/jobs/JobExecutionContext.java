package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
public class JobExecutionContext {

    private final Job job;
    private final JobEnvironment environment;

    public JobExecutionContext(Job job, JobEnvironment environment) {
        this.job = job;
        this.environment = environment;
    }

    public Job getJob() {
        return job;
    }

    public JobEnvironment getEnvironment() {
        return environment;
    }
}
