package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.*;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class LauncherJobContext extends JobExecutionContext {
    private final DocumentStore launcherDocumentStore;

    public LauncherJobContext(Job job, DocumentStore launcherDocumentStore) {
        super(job, new JobEnvironment());

        this.launcherDocumentStore = launcherDocumentStore;
    }

    public DocumentStore getLauncherDocumentStore() {
        return launcherDocumentStore;
    }
}
