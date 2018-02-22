package com.redrock.jade.cloudMama.launcher.api;

import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Copyright RedRock 2013-14
 */
@Path("/settings")
public final class SettingsResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Settings getSettings() {
        return new Settings();
    }
}
