package com.redrock.jade.cloudMama.console;

import com.redrock.jade.cloudMama.jobs.Job;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JobMetaDataTest {
    public static class SimpleJob extends Job {

        public int intToIgnore;
        public String stringToIgnore;

        @Parameter
        public int intValue;

        @Parameter
        public String stringValue;

        @Override
        protected void start() {
        }

        @Override
        protected Service getExecutor(ServiceCollection serviceCollection) {
            return null;
        }
    }

    @Test
    public void initialization_multipleParameters_allAreRegistered() throws Exception {
        JobMetaData metaData = new JobMetaData(SimpleJob.class);

        assertEquals(SimpleJob.class, metaData.getJobClass());
        Map<String, JobParameterMetaData> parameterMetaDataMap = metaData.getParameterMetaDataMap();

        assertEquals(2, parameterMetaDataMap.size());
        assertEquals(JobParameterType.INTEGER, parameterMetaDataMap.get("intValue").getType());
        assertEquals(JobParameterType.STRING, parameterMetaDataMap.get("stringValue").getType());
    }

    @Test
    public void createInstance_customValues_correctlyInitializedInstance() throws Exception {
        JobMetaData metaData = new JobMetaData(SimpleJob.class);

        Map<String, String> parameterValuesMap = new HashMap<>();
        parameterValuesMap.put("intValue", "7");
        parameterValuesMap.put("stringValue", "jade");

        SimpleJob job = (SimpleJob)metaData.createInstance(parameterValuesMap);
        assertEquals(7, job.intValue);
        assertEquals("jade", job.stringValue);
    }
}