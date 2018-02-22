package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.console.JobMetaData;
import com.redrock.jade.cloudMama.console.JobParameterType;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
public final class JobMetaDataPayload {
    private final String                                   classCanonicalName;
    private final String                                   packageName;
    private final String                                   className;
    private final Map<String, JobParameterMetaDataPayload> parameterMetaDataMap;

    public JobMetaDataPayload(JobMetaData metaData) {
        classCanonicalName = metaData.getJobClass().getCanonicalName();
        packageName = metaData.getJobClass().getPackage().getName();
        className = metaData.getJobClass().getSimpleName();

        parameterMetaDataMap = metaData.getParameterMetaDataMap()
                                       .entrySet()
                                       .stream()
                                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                                 entry -> new JobParameterMetaDataPayload(entry.getValue())));
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public Map<String, JobParameterMetaDataPayload> getParameterMetaDataMap() {
        return parameterMetaDataMap;
    }

    public String getClassCanonicalName() {
        return classCanonicalName;
    }
}
