package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.console.Parameter;
import com.redrock.jade.cloudMama.jobs.JobCallback;
import com.redrock.jade.cloudMama.launcher.api.DeploymentType;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public class NewLocalDeployment extends LauncherJob {
    @Parameter(isRequired = true)
    public String name;

    @Parameter(defaultValue = "N/A")
    public String description;

    @Parameter(defaultValue = "1.1.13")
    public String version;

    @Override
    protected void start() {
        env().log().logInfo("Starting new local deployment with name = " + name);
        startChildJob(new NewDeploymentJob(name, DeploymentType.Local), NewLocalDeployment::onDeploymentCreated);
    }

    @JobCallback
    public void onDeploymentCreated(NewDeploymentJob child) {
        env().log().logInfo("Finished calling child deployment job. Deployment: " + child.getDeployment().getId());
    }
}
