package com.redrock.jade.cloudMama.jobs;


/**
 * Copyright RedRock 2014-15
 */
public class JobEnvironment {
    private final Logger logger;

    public JobEnvironment() {
        logger = new Logger();
    }

    public Logger log() {
        return logger;
    }
}