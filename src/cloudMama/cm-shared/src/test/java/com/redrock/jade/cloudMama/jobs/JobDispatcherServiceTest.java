package com.redrock.jade.cloudMama.jobs;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.redrock.jade.cloudMama.FongoDocumentStore;
import com.redrock.jade.cloudMama.services.AkkaServiceTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobDispatcherServiceTest {
    private AkkaServiceTester    serviceTester;
    private JobExecutorService   jobExecutorService;
    private JobDispatcherService jobDispatcherService;
    private FongoDocumentStore   documentStore;

    @Before
    public void setUp() throws Exception {
        documentStore = new FongoDocumentStore();
        jobDispatcherService = new JobDispatcherService(documentStore, new FiniteDuration(100, TimeUnit.MILLISECONDS));
        jobDispatcherService.start();

        jobExecutorService = new JobExecutorService(JobExecutorServiceTest.ServiceType,
                                                    job -> new JobExecutionContext(job, new JobEnvironment()));
        jobExecutorService.start();

        serviceTester = new AkkaServiceTester();
        serviceTester.start();
    }

    @After
    public void tearDown() throws Exception {
        jobDispatcherService.stop();
        jobExecutorService.stop();
        serviceTester.stop();
    }

    private Job fetchJobAfterCompletion(JobCollection jobCollection,
                                        Job job,
                                        int timeoutSeconds) throws TimeoutException, InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Job currentJob = null;

        while (stopwatch.elapsed(TimeUnit.SECONDS) <= timeoutSeconds) {
            currentJob = jobCollection.getById(job.getId());
            if (currentJob.getStatus().isComplete()) {
                return currentJob;
            }

            Thread.sleep(100);
        }

        throw new TimeoutException(String.format("Job %s did not complete within %d seconds. Last status is: %s",
                                                 job.getId(),
                                                 timeoutSeconds,
                                                 currentJob != null ? currentJob.getStatus() : "N/A"));
    }

    @Test
    public void pipeline_successfulJobWithChildren_jobTreeSucceeds() throws Exception {
        Job job = new SuccessfulTestJobTree(true, jobExecutorService.getHostname(), jobExecutorService.getPort());
        JobCollection jobCollection = new JobCollection(documentStore);
        jobCollection.insert(job);

        job.start();

        job = fetchJobAfterCompletion(jobCollection,
                                      job,
                                      SuccessfulTestJobTree.PARALLEL_CHILDREN *
                                      (int) JobPoller.DEFAULT_POLL_DURATION.toSeconds() +
                                      5);

        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
        assertEquals(SuccessfulTestJobTree.TOTAL_CHILDREN, job.getChildren().size());
        job.getChildren()
           .forEach(childId -> assertTrue(jobCollection.getById(childId).getStatus() == ExecutionStatus.SUCCEEDED));
    }

    @Test
    public void pipeline_failedChildren_jobSucceedsWithErrorLogs() throws Exception {
        Job job = new FailedChildTestJobTree(true, jobExecutorService.getHostname(), jobExecutorService.getPort());
        JobCollection jobCollection = new JobCollection(documentStore);
        jobCollection.insert(job);

        job = fetchJobAfterCompletion(jobCollection,
                                      job,
                                      3 * (int) JobPoller.DEFAULT_POLL_DURATION.toSeconds() + 5);

        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
        assertEquals(1, job.getChildren().size());

        String childId = job.getChildren().iterator().next();
        assertEquals(ExecutionStatus.FAILED, jobCollection.getById(childId).getStatus());

        Optional<JobLogEntry> jobLog = Lists.newArrayList(new JobLogCollection(documentStore).getLogsForJob(childId))
                                            .stream()
                                            .filter(log -> log.getLevel() == LogLevel.ERROR)
                                            .findFirst();
        assertTrue(jobLog.isPresent());
    }
}