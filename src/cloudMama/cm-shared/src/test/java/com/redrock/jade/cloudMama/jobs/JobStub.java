package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

import java.util.function.BiConsumer;

/**
 * Copyright RedRock 2013-14
 */
public class JobStub extends Job {
    public boolean                         onCompleteWasCalled;
    public Job                             completedChild;
    public BiConsumer<Job, JobEnvironment> onStart;

    public JobStub() {
    }

    public JobStub(BiConsumer<Job, JobEnvironment> onStart) {
        this.onStart = onStart;
    }

    @JobCallback
    public void childCompletionHandler(Job completedChild) {
        onCompleteWasCalled = true;
        this.completedChild = completedChild;
    }

    @JobCallback
    public void childCompletionHandlerThatThrows(Job completedChild) {
        onCompleteWasCalled = true;
        this.completedChild = completedChild;
        throw new RuntimeException();
    }

    @Override
    protected void start() {
        if (onStart != null) {
            onStart.accept(this, env());
        }
    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return null;
    }
}
