package com.redrock.jade.cloudMama.console.resources;

import com.redrock.jade.cloudMama.console.JobMetaData;
import com.redrock.jade.cloudMama.console.JobParameterMetaData;
import com.redrock.jade.cloudMama.console.JobParameterType;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
public final class JobParameterMetaDataPayload {
    private final JobParameterType type;
    private final boolean isRequired;
    private final String defaultValue;

    public JobParameterMetaDataPayload(JobParameterMetaData metaData) {
        type = metaData.getType();
        isRequired = metaData.isRequired();
        defaultValue = metaData.getDefaultValue();
    }

    public JobParameterType getType() {
        return type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
