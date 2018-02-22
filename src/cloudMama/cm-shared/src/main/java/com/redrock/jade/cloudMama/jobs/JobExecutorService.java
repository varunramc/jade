package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.cloudMama.services.AkkaRemoteService;

/**
 * Copyright RedRock 2013-14
 */
public class JobExecutorService extends AkkaRemoteService {
    public static final String REQUEST_ROUTER_ACTOR_NAME = "ExecutionRequestRouter";

    private final JobExecutionContextCreator contextCreator;

    public JobExecutorService(String serviceType, JobExecutionContextCreator contextCreator) {
        super(serviceType);

        this.contextCreator = contextCreator;
    }

    @Override
    public void start() {
        super.start();

        actorSystem.actorOf(JobExecutionRequestRouter.getProps(contextCreator),
                            JobExecutorService.REQUEST_ROUTER_ACTOR_NAME);
    }
}
