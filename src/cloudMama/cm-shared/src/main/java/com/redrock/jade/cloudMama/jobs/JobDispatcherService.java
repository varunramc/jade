package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.shared.dao.DocumentStore;
import com.redrock.jade.cloudMama.services.AkkaRemoteService;
import scala.concurrent.duration.FiniteDuration;


/**
 * Copyright RedRock 2013-14
 */
public final class JobDispatcherService extends AkkaRemoteService {
    private static final String ServiceType = "JobDispatcher";

    private final DocumentStore  documentStore;
    private final FiniteDuration jobPollInterval;

    public JobDispatcherService(DocumentStore documentStore) {
        this(documentStore, JobPoller.DEFAULT_POLL_DURATION);
    }

    public JobDispatcherService(DocumentStore documentStore, FiniteDuration jobPollInterval) {
        super(JobDispatcherService.ServiceType);
        this.documentStore = documentStore;
        this.jobPollInterval = jobPollInterval;
    }

    @Override
    public void start() {
        super.start();

        actorSystem.actorOf(JobPoller.getProps(documentStore,
                                               JobDispatcher.getProps(documentStore),
                                               jobPollInterval));
    }
}
