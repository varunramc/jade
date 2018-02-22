package com.redrock.jade.cloudMama.console;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Copyright RedRock 2013-14
 */
public class ContentServer {
    private final Logger logger = LoggerFactory.getLogger(ContentServer.class);

    private HttpServer               server;
    private NetworkListener          networkListener;
    private ConfigurationHttpHandler configHttpHandler;

    public void start() throws IOException {
        logger.info("Starting server");

        server = new HttpServer();

        networkListener = new NetworkListener("ContentServer", "0.0.0.0", 0);
        server.addListener(networkListener);

        addHttpHandlers();

        logger.info("Server URL: " + getUrl());
    }

    private void addHttpHandlers() throws IOException {
        CLStaticHttpHandler
                staticHttpHandler = new CLStaticHttpHandler(ContentServer.class.getClassLoader());
        staticHttpHandler.setFileCacheEnabled(false);   // TMP: For development only
        server.getServerConfiguration().addHttpHandler(staticHttpHandler,
                                                       "/");

        configHttpHandler = new ConfigurationHttpHandler();
        server.getServerConfiguration().addHttpHandler(configHttpHandler, "/__config.js");
        server.start();
    }

    public void stop() {
        if (server != null) {
            logger.info("Shutting down server");

            server.shutdownNow();
            server = null;
        }
    }

    public JsClientConfiguration getClientConfiguration() {
        return configHttpHandler.getClientConfiguration();
    }

    public String getUrl() {
        try {
            return String.format("http://%s:%d/",
                                 InetAddress.getLocalHost().getHostAddress(),
                                 networkListener.getPort());
        }
        catch (UnknownHostException ex) {
            return String.format("(EXCEPTION: %s)", ex.toString());
        }
    }
}
