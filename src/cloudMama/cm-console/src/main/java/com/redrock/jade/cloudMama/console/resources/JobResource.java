package com.redrock.jade.cloudMama.console.resources;

import com.google.common.collect.Lists;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.cloudMama.console.JobLibrary;
import com.redrock.jade.cloudMama.console.JobMetaData;
import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.jobs.JobCollection;
import com.redrock.jade.cloudMama.jobs.JobLogCollection;
import com.redrock.jade.cloudMama.jobs.JobLogEntry;
import com.redrock.jade.shared.dao.DocumentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copyright RedRock 2013-14
 */
public final class JobResource {
    private final Logger logger = LoggerFactory.getLogger(JobResource.class);

    private final JobLibrary    jobLibrary;
    private final DocumentStore documentStore;

    public JobResource(JobLibrary jobLibrary, DocumentStore documentStore) {
        this.jobLibrary = jobLibrary;
        this.documentStore = documentStore;
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobPayload getJob(@PathParam("jobId") String jobId) {
        JobCollection jobCollection = new JobCollection(documentStore);
        Job job = jobCollection.getById(jobId);

        if (job != null) {
            return new JobPayload(job, jobCollection);
        }
        else {
            throw new NotFoundException("Could not find job: " + jobId);
        }
    }

    @GET
    @Path("/{jobId}/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<JobLogPayload> getJobLogs(@PathParam("jobId") String jobId) {
        JobLogCollection logCollection = new JobLogCollection(documentStore);

        return StreamUtils.stream(logCollection.getLogsForJob(jobId)).map(JobLogPayload::new)::iterator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public JobPayload createJob(JobInputMessage jobInputMessage) throws Exception {
        logger.info("Request for new job. Details: " + jobInputMessage);

        try {
            return new JobPayload(createJobFromInput(jobInputMessage), null);
        }
        catch (Throwable throwable) {
            String errorMessage = "Failed to spawn job: " + throwable;
            throw new WebApplicationException("Failed to spawn job",
                                              throwable,
                                              Response.serverError().entity(errorMessage).build());
        }
    }

    private Job createJobFromInput(JobInputMessage jobInputMessage) throws Exception {
        JobMetaData jobMetaData = jobLibrary.getMetaDataByClassName(jobInputMessage.getClassCanonicalName());
        Job job = jobMetaData.createInstance(jobInputMessage.getParameters());
        new JobCollection(documentStore).insert(job);

        return job;
    }
}
