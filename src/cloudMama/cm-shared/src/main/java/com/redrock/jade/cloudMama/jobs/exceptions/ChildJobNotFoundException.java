package com.redrock.jade.cloudMama.jobs.exceptions;

/**
 * Copyright RedRock 2013-14
 */
public final class ChildJobNotFoundException extends RuntimeException {
    public ChildJobNotFoundException(String message) {
        super(message);
    }
}
