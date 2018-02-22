package com.redrock.jade.cloudMama.jobs;

import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class JobCollection extends DocumentCollection<Job> {

    public JobCollection(DocumentStore documentStore) {
        super(documentStore, Job.class);
    }

    public Iterable<Job> getJobsPendingExecution() {
        return collection.find("{ $or: [{status: #}, {status: #}] }",
                               ExecutionStatus.PENDING,
                               ExecutionStatus.AWAITING_CHILD_COMPLETION)
                         .as(documentClass);
    }
}
