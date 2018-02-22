package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.redrock.jade.cloudMama.AkkaUtils;
import com.redrock.jade.cloudMama.RoleInstance;
import com.redrock.jade.cloudMama.services.AkkaServiceTester;
import com.redrock.jade.cloudMama.services.RoleService;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobExecutorServiceTest {
    public static final String ServiceType = "TestJobExecutor";

    private static class SerializableServiceJob extends Job {
        @Override
        protected void start() {
        }

        @Override
        protected Service getExecutor(ServiceCollection serviceCollection) {
            return null;
        }
    }

    private JobExecutorService jobExecutorService;
    private AkkaServiceTester  serviceTester;

    @Before
    public void setUp() throws Exception {
        jobExecutorService = new JobExecutorService(JobExecutorServiceTest.ServiceType,
                                                    job -> new JobExecutionContext(job, new JobEnvironment()));
        jobExecutorService.start();

        serviceTester = new AkkaServiceTester();
        serviceTester.start();
    }

    @After
    public void tearDown() throws Exception {
        serviceTester.stop();
        jobExecutorService.stop();
    }

    @Test
    public void testJobExecution() throws Exception {
        Job job = new SerializableServiceJob();
        StartJobMessage message = new StartJobMessage(job);

        Service service = new RoleService(new RoleInstance(),
                                          JobExecutorServiceTest.ServiceType,
                                          jobExecutorService.getHostname(),
                                          jobExecutorService.getPort());
        String remoteServiceActorPath =
                AkkaUtils.getRemoteServiceActorPath(service, JobExecutorService.REQUEST_ROUTER_ACTOR_NAME);

        ActorSelection actorSelection = serviceTester.getActorSystem().actorSelection(remoteServiceActorPath);
        Future<Object> ask =
                Patterns.ask(actorSelection, message, Timeout.durationToTimeout(Duration.create(1, TimeUnit.SECONDS)));

        JobProgressMessage progressMessage = (JobProgressMessage) Await.result(ask, Duration.create(1, TimeUnit.SECONDS));
        assertEquals(ExecutionStatus.SUCCEEDED, progressMessage.getJob().getStatus());
    }
}