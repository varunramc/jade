package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.launcher.LauncherService;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public abstract class LauncherJob extends Job {
    protected final LauncherJobEnvironment env() {
        return (LauncherJobEnvironment)super.env();
    }

    @Override
    protected final Service getExecutor(ServiceCollection serviceCollection) {
        LauncherJobExecutorService executorService = LauncherJobExecutorService.getInstance();
        return new LauncherService(executorService.getServiceType(),
                                   executorService.getHostname(),
                                   executorService.getPort());
    }
}
