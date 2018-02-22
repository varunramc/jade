package com.redrock.jade.cloudMama.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * Copyright RedRock 2013-14
 */
final class ConfigurationHttpHandler extends HttpHandler {
    private final JsClientConfiguration clientConfiguration;

    public ConfigurationHttpHandler() {
        clientConfiguration = new JsClientConfiguration();
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        response.setContentType("application/javascript");

        String configuration = String.format("var __clientCfg = %s;", getConfigurationJson());
        response.getWriter().write(configuration);
    }

    private String getConfigurationJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(clientConfiguration.getConfigurationMap());
    }

    public JsClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }
}
