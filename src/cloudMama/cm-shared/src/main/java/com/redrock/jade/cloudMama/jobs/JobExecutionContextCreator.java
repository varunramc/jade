package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
@FunctionalInterface
public interface JobExecutionContextCreator {
    JobExecutionContext create(Job job);
}
