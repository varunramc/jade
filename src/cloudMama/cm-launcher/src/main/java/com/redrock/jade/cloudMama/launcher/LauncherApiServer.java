package com.redrock.jade.cloudMama.launcher;

import com.redrock.jade.cloudMama.FongoDocumentStore;
import com.redrock.jade.cloudMama.console.CorsResponseFilter;
import com.redrock.jade.cloudMama.console.JobLibrary;
import com.redrock.jade.cloudMama.console.resources.JobsResource;
import com.redrock.jade.cloudMama.jobs.JobDispatcherService;
import com.redrock.jade.cloudMama.launcher.api.SettingsResource;
import com.redrock.jade.cloudMama.launcher.jobs.LauncherJobExecutorService;
import com.redrock.jade.cloudMama.launcher.jobs.NewLocalDeployment;
import com.redrock.jade.cloudMama.launcher.api.DeploymentResource;
import com.redrock.jade.shared.dao.DocumentStore;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Copyright RedRock 2013-14
 */
public final class LauncherApiServer {
    public static final  String BASE_URI_TEMPLATE     = "http://%s:%d/api/";
    private static final URI    BASE_URI              = URI.create(String.format(BASE_URI_TEMPLATE, "0.0.0.0", 0));
    private static final String GRIZZLY_LISTENER_NAME = "grizzly";

    private final Logger logger = LoggerFactory.getLogger(LauncherApiServer.class);

    private HttpServer           server;
    private JobLibrary           jobLibrary;
    private DocumentStore        documentStore;
    private JobDispatcherService jobDispatcherService;

    public void start() {
        logger.info("Starting server");

        documentStore = new FongoDocumentStore();
        jobLibrary = new JobLibrary();
        jobLibrary.registerPackage(NewLocalDeployment.class.getPackage());

        startJobServices();
        startRestServer();
    }

    private void startJobServices() {
        jobDispatcherService = new JobDispatcherService(documentStore);
        jobDispatcherService.start();

        LauncherJobExecutorService.initialize(documentStore);
        LauncherJobExecutorService.getInstance().start();
    }

    private void startRestServer() {
        ResourceConfig resourceConfig = new ResourceConfig();
        registerResourceClasses(resourceConfig);
        resourceConfig.register(CorsResponseFilter.class);

        server = GrizzlyHttpServerFactory.createHttpServer(LauncherApiServer.BASE_URI, resourceConfig);
        logApiBaseUri();
    }

    private void registerResourceClasses(ResourceConfig resourceConfig) {
        resourceConfig.register(DeploymentResource.class);
        resourceConfig.register(JobsResource.class);
        resourceConfig.register(SettingsResource.class);

        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(jobLibrary).to(JobLibrary.class);
                bind(documentStore).to(DocumentStore.class);
            }
        });
    }

    private void logApiBaseUri() {
        try {
            logger.info("Server base URL: " + String.format(LauncherApiServer.BASE_URI_TEMPLATE,
                                                            InetAddress.getLocalHost().getHostAddress(),
                                                            getPort()));
        }
        catch (UnknownHostException ex) {
            logger.error("Failed to determine host address. Details: " + ex);
        }
        catch (OperationNotSupportedException ex) {
            logger.error("Unexpected exception while determining API base URI. Details: " + ex);
        }
    }

    public void stop() {
        if (server != null) {
            logger.info("Shutting down server");

            server.shutdownNow();
            server = null;

            LauncherJobExecutorService.getInstance().stop();
            jobDispatcherService.stop();
        }
    }

    public int getPort() throws OperationNotSupportedException {
        if (server != null) {
            return server.getListener(LauncherApiServer.GRIZZLY_LISTENER_NAME).getPort();
        }

        throw new OperationNotSupportedException("Server is not running");
    }
}
