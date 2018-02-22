package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.JobEnvironment;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class LauncherJobEnvironment extends JobEnvironment {
    private final DocumentStore launcherDocumentStore;

    public LauncherJobEnvironment(DocumentStore launcherDocumentStore) {
        this.launcherDocumentStore = launcherDocumentStore;
    }

    public DocumentStore getLauncherDocumentStore() {
        return launcherDocumentStore;
    }
}
