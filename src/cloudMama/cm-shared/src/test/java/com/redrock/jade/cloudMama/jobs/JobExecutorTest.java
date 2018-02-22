package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobExecutorTest {
    private ActorSystem               actorSystem;
    private TestActorRef<JobExecutor> actorRef;

    @Before
    public void setUp() throws Exception {
        actorSystem = ActorSystem.apply();
        Props props = JobExecutor.getProps();
        actorRef = TestActorRef.create(actorSystem, props, "test");
    }

    @After
    public void tearDown() throws Exception {
        actorSystem.shutdown();
    }

    @Test
    public void onReceive_startJobMessage_startsJob() throws Exception {
        final boolean[] startCalled = {false};
        Job job = new JobStub((j, env) -> startCalled[0] = true);
        JobExecutionContext context = new JobExecutionContext(job, new JobEnvironment());

        ExecuteNewJobMessage message = new ExecuteNewJobMessage(context);
        actorRef.tell(message, null);

        assertTrue(startCalled[0]);
        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
    }

    @Test
    public void onReceive_resumeJobMessage_jobResumed() throws Exception {
        Job child = new JobStub();
        JobStub job = new JobStub();
        job.startChildJob(child, JobStub::childCompletionHandler);

        JobExecutionContext context = new JobExecutionContext(job, new JobEnvironment());

        ExecuteJobOnChildrenCompletionMessage message =
                new ExecuteJobOnChildrenCompletionMessage(context, Arrays.asList(child));
        actorRef.tell(message, null);

        assertTrue(job.onCompleteWasCalled);
        assertEquals(child.getId(), job.completedChild.getId());
        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
    }
}