package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
 * Copyright RedRock 2013-14
 */
public class NoPublicEmptyConstructorJobStub extends Job {
    NoPublicEmptyConstructorJobStub() {

    }

    NoPublicEmptyConstructorJobStub(int a) {

    }

    @JobCallback
    public void childCompletionHandler(Job completedChild) {
    }

    @Override
    protected void start() {
        startChildJob(new JobStub(), NoPublicEmptyConstructorJobStub::childCompletionHandler);
    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return null;
    }
}
