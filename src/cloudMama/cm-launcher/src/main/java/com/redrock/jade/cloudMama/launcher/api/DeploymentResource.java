package com.redrock.jade.cloudMama.launcher.api;

import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

/**
 * Copyright RedRock 2013-14
 */
@Path("/deployments")
public final class DeploymentResource {
    @Inject
    private DocumentStore documentStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<Deployment> getDeployments() {
        return new DocumentCollection<>(documentStore, Deployment.class).getAll();
    }
}
