package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.JobExecutionContext;
import com.redrock.jade.cloudMama.jobs.JobExecutorService;
import com.redrock.jade.cloudMama.launcher.LauncherService;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class LauncherJobExecutorService extends JobExecutorService {
    private static final String ServiceType = "LauncherJobExecutor";
    private static LauncherJobExecutorService instance;

    private LauncherJobExecutorService(DocumentStore documentStore) {
        super(LauncherJobExecutorService.ServiceType,
              job -> new JobExecutionContext(job, new LauncherJobEnvironment(documentStore)));
    }

    public static void initialize(DocumentStore documentStore) {
        if (LauncherJobExecutorService.instance == null) {
            LauncherJobExecutorService.instance = new LauncherJobExecutorService(documentStore);
        }
    }

    public static LauncherJobExecutorService getInstance() {
        return LauncherJobExecutorService.instance;
    }

    public LauncherService getService() {
        return new LauncherService(getServiceType(), getHostname(), getPort());
    }
}
