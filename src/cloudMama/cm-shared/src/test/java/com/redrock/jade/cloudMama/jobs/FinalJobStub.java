package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public final class FinalJobStub extends Job {
    @JobCallback
    public void childCompletionHandler(Job completedChild) {
    }

    @Override
    protected void start() {
        startChildJob(new JobStub(), FinalJobStub::childCompletionHandler);
    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return null;
    }
}
