package com.redrock.jade.cloudMama.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Copyright RedRock 2013-14
 */
public final class CorsResponseFilter implements ContainerResponseFilter {

    private final Logger logger = LoggerFactory.getLogger(CorsResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "content-type");
        responseContext.getHeaders().add("Access-Control-Expose-Headers", "location");
    }
}
