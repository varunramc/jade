package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobExecutionRequestRouterTest {
    private ActorSystem actorSystem;

    @Before
    public void setUp() {
        actorSystem = ActorSystem.apply();
    }

    @After
    public void tearDown() {
        actorSystem.shutdown();
    }

    @Test
    public void onReceive_startJobMessage_createsExecuteNewJobMessage() throws Exception {
        JobStub job = new JobStub();
        StartJobMessage message = new StartJobMessage(job);

        new JavaTestKit(actorSystem) {
            {
                Props props = JobExecutionRequestRouter.getProps(j -> new JobExecutionContext(j, new JobEnvironment()));
                ActorRef testActor = getSystem().actorOf(props);

                watch(testActor);
                testActor.tell(message, getRef());

                JobProgressMessage progressMessage =
                        expectMsgClass(Duration.create(1, TimeUnit.SECONDS), JobProgressMessage.class);

                assertEquals(ExecutionStatus.SUCCEEDED, progressMessage.getJob().getStatus());
            }
        };
    }

    @Test
    public void onReceive_resumeJobMessage_createsExecuteNewJobMessage() throws Exception {
        JobStub child = new JobStub();
        JobStub job = new JobStub();
        job.startChildJob(child);

        ResumeJobOnChildrenCompletionMessage message =
                new ResumeJobOnChildrenCompletionMessage(job, Arrays.asList(child));

        new JavaTestKit(actorSystem) {
            {
                Props props = JobExecutionRequestRouter.getProps(j -> new JobExecutionContext(j, new JobEnvironment()));
                ActorRef testActor = getSystem().actorOf(props);

                watch(testActor);
                testActor.tell(message, getRef());

                JobProgressMessage progressMessage =
                        expectMsgClass(Duration.create(1, TimeUnit.SECONDS), JobProgressMessage.class);

                assertEquals(ExecutionStatus.SUCCEEDED, progressMessage.getJob().getStatus());
            }
        };
    }
}