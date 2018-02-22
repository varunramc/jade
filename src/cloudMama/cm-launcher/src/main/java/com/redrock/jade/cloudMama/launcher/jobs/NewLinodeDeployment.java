package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.console.Parameter;
import com.redrock.jade.cloudMama.launcher.LauncherService;
import com.redrock.jade.cloudMama.launcher.api.DeploymentType;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public class NewLinodeDeployment extends LauncherJob {
    @Parameter
    public String name;

    @Parameter(defaultValue = "4")
    public int initialHostCount;

    @Override
    protected void start() {
        env().log().logInfo("Name = " + name);
        env().log().logInfo("Host count = " + initialHostCount);

        startChildJob(new LongRunningJob(), NewLinodeDeployment::onChildComplete);
    }

    public void onChildComplete(LongRunningJob job) {
        env().log().logInfo("Long running child finished with status = " + job.getStatus());
    }
/*
    @Override
    protected final Service getExecutor(ServiceCollection serviceCollection) {
        LauncherJobExecutorService executorService = LauncherJobExecutorService.getInstance();
        return new LauncherService(executorService.getServiceType(),
                                   executorService.getHostname(),
                                   executorService.getPort());
    }*/
}
