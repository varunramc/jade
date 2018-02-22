package com.redrock.jade.cloudMama.console;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright RedRock 2013-14
 */
public final class JsClientConfiguration {
    private final Map<String, Object> configMap;

    public JsClientConfiguration() {
        configMap = new HashMap<>();
    }

    public void addConfiguration(String key, Object value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        configMap.put(key, value);
    }

    Map<String, Object> getConfigurationMap() {
        return configMap;
    }
}
