package com.redrock.jade.cloudMama.jobs;

import com.google.common.collect.Lists;
import com.redrock.jade.cloudMama.FongoDocumentStore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public final class JobCollectionTest {
    @Test
    public void getJobsReadyToExecute_Pending_Returned() {
        testGetJobsReadyToExecuteWithStatus(ExecutionStatus.PENDING);
    }

    @Test
    public void getJobsReadyToExecute_Waiting_Returned() {
        testGetJobsReadyToExecuteWithStatus(ExecutionStatus.AWAITING_CHILD_COMPLETION);
    }

    private void testGetJobsReadyToExecuteWithStatus(ExecutionStatus executionStatusToTest) {
        FongoDocumentStore documentStore = new FongoDocumentStore();
        JobCollection jobCollection = new JobCollection(documentStore);

        Job job = new JobStub();
        job.setStatus(executionStatusToTest);
        jobCollection.insert(job);

        List<Job> jobsReadyToExecute = Lists.newArrayList(jobCollection.getJobsPendingExecution());
        assertEquals(1, jobsReadyToExecute.size());
        assertEquals(executionStatusToTest, jobsReadyToExecute.get(0).getStatus());
    }
}