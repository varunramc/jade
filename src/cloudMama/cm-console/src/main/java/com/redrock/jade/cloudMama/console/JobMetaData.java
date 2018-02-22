package com.redrock.jade.cloudMama.console;

import com.redrock.jade.cloudMama.jobs.Job;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright RedRock 2013-14
 */
public final class JobMetaData {
    private final Class<? extends Job>              jobClass;
    private final Map<String, JobParameterMetaData> parameterMetaDataMap;

    public JobMetaData(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;

        parameterMetaDataMap = new HashMap<>();
        buildFieldMap(jobClass);
    }

    private void buildFieldMap(Class<? extends Job> jobClass) {
        for (Field field : jobClass.getFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                parameterMetaDataMap.put(field.getName(), new JobParameterMetaData(field));
            }
        }
    }

    public Job createInstance(Map<String, String> parameterValues) throws Exception {
        Job job = jobClass.newInstance();

        for (String key : parameterValues.keySet()) {
            if (!parameterMetaDataMap.containsKey(key)) {
                throw new IllegalArgumentException(String.format(
                        "The job class '%s' does not contain a parameter with name '%s'",
                        jobClass.getName(),
                        key));
            }
            parameterMetaDataMap.get(key).setValue(job, parameterValues.get(key));
        }

        return job;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public Map<String, JobParameterMetaData> getParameterMetaDataMap() {
        return parameterMetaDataMap;
    }
}
