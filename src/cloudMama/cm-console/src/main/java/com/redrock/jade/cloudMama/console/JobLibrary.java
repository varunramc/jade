package com.redrock.jade.cloudMama.console;

import com.google.common.base.Preconditions;
import com.redrock.jade.cloudMama.jobs.Job;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Copyright RedRock 2013-14
 */
public final class JobLibrary {
    private final Map<String, JobMetaData> jobMetaDataMap;

    public JobLibrary() {
        jobMetaDataMap = new HashMap<>();
    }

    public void registerPackage(Package jobPackage) {
        Preconditions.checkNotNull(jobPackage);

        Reflections reflections = new Reflections(jobPackage.getName());
        for (Class<? extends Job> jobClass : reflections.getSubTypesOf(Job.class)) {
            registerJobClass(jobClass);
        }
    }

    private void registerJobClass(Class<? extends Job> jobClass) {
        JobMetaData metaData = new JobMetaData(jobClass);
        jobMetaDataMap.put(jobClass.getCanonicalName(), metaData);
    }

    public Stream<JobMetaData> getAllDiscoveredMetaData() {
        return jobMetaDataMap.values().stream();
    }

    public JobMetaData getMetaDataByClassName(String jobClassName) {
        if (!jobMetaDataMap.containsKey(jobClassName)) {
            throw new IllegalArgumentException("Unknown job class name: " + jobClassName);
        }

        return jobMetaDataMap.get(jobClassName);
    }
}
