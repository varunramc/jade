package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.console.JobMetaData;
import com.redrock.jade.cloudMama.console.JobParameterType;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
public final class JobInputMessage {
    private String              classCanonicalName;
    private Map<String, String> parameters;

    public String getClassCanonicalName() {
        return classCanonicalName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "JobInputMessage{" +
               "classCanonicalName='" + classCanonicalName + '\'' +
               ", parameters=" + parameters +
               '}';
    }
}
