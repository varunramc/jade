package com.redrock.jade.cloudMama.jobs;

/**
 * Copyright RedRock 2013-14
 */
final class StartJobMessage extends JobMessage {
    public StartJobMessage(Job job) {
        super(job);
    }

    public StartJobMessage() {
        super(null);
    }
}
