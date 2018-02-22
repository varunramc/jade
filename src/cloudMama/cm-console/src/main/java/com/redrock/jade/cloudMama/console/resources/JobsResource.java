package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.console.JobLibrary;
import com.redrock.jade.cloudMama.console.JobMetaData;
import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.jobs.JobCollection;
import com.redrock.jade.shared.dao.DocumentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
@Path("/jobs")
public final class JobsResource {
    private final Logger logger = LoggerFactory.getLogger(JobsResource.class);

    @Inject
    private JobLibrary jobLibrary;

    @Inject
    private DocumentStore documentStore;

    @GET
    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<JobMetaDataPayload> getDiscoveredMetaData(@QueryParam("packageName") String packageName) {
        return jobLibrary.getAllDiscoveredMetaData()
                         .filter(metaData -> metaData.getJobClass().getPackage().getName().equals(packageName))
                         .map(JobMetaDataPayload::new)
                         .collect(Collectors.toList());
    }

    @Path("/job")
    public JobResource getJobResource() {
        return new JobResource(jobLibrary, documentStore);
    }
}
