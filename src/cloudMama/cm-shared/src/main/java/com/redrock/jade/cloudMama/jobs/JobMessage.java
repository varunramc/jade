package com.redrock.jade.cloudMama.jobs;

import java.io.Serializable;

/**
 * Copyright RedRock 2013-14
 */
abstract class JobMessage implements Serializable {
    private final Job job;

    protected JobMessage(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
