package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.RoleInstance;
import com.redrock.jade.cloudMama.services.RoleService;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

import javax.naming.OperationNotSupportedException;

/**
* Copyright RedRock 2013-14
*/
public class FailedChildTestJobTree extends Job {
    private final String  jobExecutorAddress;
    private final int     jobExecutorPort;
    private final boolean isRoot;

    FailedChildTestJobTree() {
        jobExecutorAddress = null;
        jobExecutorPort = 0;
        isRoot = false;
    }

    public FailedChildTestJobTree(boolean isRoot, String jobExecutorAddress, int jobExecutorPort) {
        this.isRoot = isRoot;
        this.jobExecutorAddress = jobExecutorAddress;
        this.jobExecutorPort = jobExecutorPort;
    }

    @Override
    protected void start() throws Exception {
        if (isRoot) {
            startChildJob(new FailedChildTestJobTree(false, jobExecutorAddress, jobExecutorPort));
        }
        else {
            throw new OperationNotSupportedException();
        }
    }

    @Override
    protected Service getExecutor(ServiceCollection serviceCollection) {
        return new RoleService(new RoleInstance(),
                               JobExecutorServiceTest.ServiceType,
                               jobExecutorAddress,
                               jobExecutorPort);
    }
}
