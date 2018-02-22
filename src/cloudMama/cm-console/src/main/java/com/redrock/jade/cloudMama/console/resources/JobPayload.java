package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.jobs.ExecutionStatus;
import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.jobs.JobCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
public final class JobPayload {
    private final String           id;
    private final String           type;
    private final ExecutionStatus  status;
    private final float            progressPercentage;
    private final String           error;
    private final List<JobPayload> children;

    public JobPayload(Job job, JobCollection jobCollection) {
        id = job.getId();
        type = job.getClass().getCanonicalName();
        status = job.getStatus();
        progressPercentage = job.getProgressPercentage();
        error = job.getError();

        children = new ArrayList<>();
        for (String childId : job.getChildren()) {
            Job child = jobCollection.getById(childId);
            if (child != null) {
                children.add(new JobPayload(child, jobCollection));
            }
        }
        /*
        children = job.getChildren()
                      .stream()
                      .map(jobCollection::getById)
                      .filter(child -> child != null)
                      .map(child -> new JobPayload(child, jobCollection))
                      .collect(Collectors.toList());*/
    }

    public String getId() {
        return id;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Collection<JobPayload> getChildren() {
        return children;
    }

    public String getType() {
        return type;
    }

    public float getProgressPercentage() {
        return progressPercentage;
    }

    public String getError() {
        return error;
    }
}
