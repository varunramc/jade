package com.redrock.jade.cloudMama.launcher.jobs;

import com.redrock.jade.cloudMama.launcher.api.Deployment;
import com.redrock.jade.cloudMama.launcher.api.DeploymentType;
import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public class NewDeploymentJob extends LauncherJob {
    private String         deploymentName;
    private DeploymentType type;
    private Deployment     deployment;

    public NewDeploymentJob() {
    }

    public NewDeploymentJob(String deploymentName, DeploymentType type) {
        this.deploymentName = deploymentName;
        this.type = type;
    }

    @Override
    protected void start() {
        env().log().logInfo(String.format("Creating deployment of type '%s' and name '%s'",
                                          type,
                                          deploymentName));

        deployment = new Deployment(type, deploymentName);
        new DocumentCollection<>(env().getLauncherDocumentStore(), Deployment.class).insert(deployment);

        env().log().logInfo("Done creating deployment!");
    }

    public Deployment getDeployment() {
        return deployment;
    }
}
