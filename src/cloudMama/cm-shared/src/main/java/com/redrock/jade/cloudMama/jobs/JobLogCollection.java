package com.redrock.jade.cloudMama.jobs;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

import java.util.stream.Stream;

/**
 * Copyright RedRock 2013-14
 */
public final class JobLogCollection extends DocumentCollection<JobLogEntry> {

    public JobLogCollection(DocumentStore documentStore) {
        super(documentStore, JobLogEntry.class);
    }

    public Iterable<JobLogEntry> getLogsForJob(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        return collection.find("{job: {id: #}}", id).as(documentClass);
    }
}
