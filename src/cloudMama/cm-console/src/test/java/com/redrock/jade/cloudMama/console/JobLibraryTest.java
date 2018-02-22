package com.redrock.jade.cloudMama.console;

import com.redrock.jade.cloudMama.console.jobs.EmptyJob;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

public class JobLibraryTest {

    @Test
    public void testGetAllDiscoveredMetaData() throws Exception {
        JobLibrary library = new JobLibrary();
        library.registerPackage(EmptyJob.class.getPackage());

        Stream<JobMetaData> jobMetaDataStream = library.getAllDiscoveredMetaData();

        assertTrue(jobMetaDataStream.anyMatch(metaData -> metaData.getJobClass().equals(EmptyJob.class)));
    }

    @Test
    public void testGetMetaDataByClassName() throws Exception {
        JobLibrary library = new JobLibrary();
        library.registerPackage(EmptyJob.class.getPackage());

        JobMetaData metaData = library.getMetaDataByClassName(EmptyJob.class.getCanonicalName());

        assertEquals(EmptyJob.class, metaData.getJobClass());
    }
}