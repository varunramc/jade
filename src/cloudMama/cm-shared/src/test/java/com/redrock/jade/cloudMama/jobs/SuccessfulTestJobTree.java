package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.RoleInstance;
import com.redrock.jade.cloudMama.services.RoleService;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;

/**
* Copyright RedRock 2013-14
*/
public class SuccessfulTestJobTree extends Job {
    public static final int PARALLEL_CHILDREN = 5;
    public static final int TOTAL_CHILDREN = PARALLEL_CHILDREN + 1;

    private final String  jobExecutorAddress;
    private final int     jobExecutorPort;
    private final boolean isRoot;

    public SuccessfulTestJobTree() {
        jobExecutorAddress = null;
        jobExecutorPort = 0;
        isRoot = false;
    }

    public SuccessfulTestJobTree(boolean isRoot, String jobExecutorAddress, int jobExecutorPort) {
        this.isRoot = isRoot;
        this.jobExecutorAddress = jobExecutorAddress;
        this.jobExecutorPort = jobExecutorPort;
    }

    @Override
    protected void start() {
        if (isRoot) {
            startChildJob(new SuccessfulTestJobTree(false, jobExecutorAddress, jobExecutorPort),
                          SuccessfulTestJobTree::onFirstChildCompleted);
        }
    }

    @JobCallback
    public void onFirstChildCompleted(SuccessfulTestJobTree child) {
        env().log().logInfo("Finished child: " + child);

        for (int i = 0; i < SuccessfulTestJobTree.PARALLEL_CHILDREN; i++) {
            startChildJob(new SuccessfulTestJobTree(false, jobExecutorAddress, jobExecutorPort));
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
