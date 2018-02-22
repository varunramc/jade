package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.common.collect.Lists;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class JobExecutionMonitorTest {
    @Test
    public void onReceive_executeNewJobMessage_createsExecutorAndForwardsMessage() throws Exception {
        Job job = new JobStub();
        JobExecutionContext context = new JobExecutionContext(job, new JobEnvironment());
        testExecuteJobMessageHandling(new ExecuteNewJobMessage(context), ExecuteNewJobMessage.class);
    }

    @Test
    public void onReceive_executeWaitingJobMessage_createsExecutorAndForwardsMessage() throws Exception {
        Job job = new JobStub();
        Job child = new JobStub();
        job.startChildJob(child);

        JobExecutionContext context = new JobExecutionContext(job, new JobEnvironment());
        testExecuteJobMessageHandling(new ExecuteJobOnChildrenCompletionMessage(context, Arrays.asList(child)),
                                      ExecuteJobOnChildrenCompletionMessage.class);
    }

    private <T extends ExecuteJobMessage> void testExecuteJobMessageHandling(T msg, Class<T> msgClass) {
        ActorSystem actorSystem = ActorSystem.apply();

        new JavaTestKit(actorSystem) {
            {
                Props props = JobExecutionMonitor.getProps(MessageForwarderActor.getProps(getRef()),
                                                           new FiniteDuration(10, TimeUnit.MILLISECONDS));
                ActorRef testActor = getSystem().actorOf(props);

                watch(testActor);
                testActor.tell(msg, getRef());

                T receivedMsg =
                        expectMsgClass(Duration.create(1, TimeUnit.SECONDS), msgClass);
                assertEquals(msg.getJobExecutionContext().getJob().getId(),
                             receivedMsg.getJobExecutionContext().getJob().getId());

                shutdown(getSystem());
            }
        };
    }

    @Test
    public void onReceive_newJobCompleting_progressMessageSentAndActorShutsDown() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        JobEnvironment environment = new JobEnvironment();
        Job job = new JobStub((j, env) -> env.log().logVerbose("verbose"));
        job.startJob(environment);

        JobExecutionContext context = new JobExecutionContext(job, environment);
        ExecuteNewJobMessage message = new ExecuteNewJobMessage(context);

        new JavaTestKit(actorSystem) {
            {
                Props props = JobExecutionMonitor.getProps(MessageForwarderActor.getProps(getRef()),
                                                           new FiniteDuration(10, TimeUnit.MILLISECONDS));
                ActorRef testActor = getSystem().actorOf(props);

                watch(testActor);
                testActor.tell(message, getRef());

                expectMsgClass(ExecuteNewJobMessage.class);
                JobProgressMessage progressMessage =
                        expectMsgClass(Duration.create(1, TimeUnit.SECONDS), JobProgressMessage.class);

                ArrayList<LogEntry> logEntries = Lists.newArrayList(progressMessage.getLogEntries());
                assertEquals(1, logEntries.size());
                assertEquals(LogLevel.VERBOSE, logEntries.get(0).getLevel());
                assertEquals(ExecutionStatus.SUCCEEDED, progressMessage.getJob().getStatus());

                expectTerminated(Duration.create(1, TimeUnit.SECONDS), testActor);

                shutdown(getSystem());
            }
        };
    }

    @Test
    public void onReceive_newJobSpawningChildAndCompleting_progressMessageSentAndActorShutsDown() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        JobEnvironment environment = new JobEnvironment();
        Job child = new JobStub();
        Job job = new JobStub((j, env) -> j.startChildJob(child));
        job.startJob(environment);

        JobExecutionContext context = new JobExecutionContext(job, environment);
        ExecuteNewJobMessage message = new ExecuteNewJobMessage(context);

        new JavaTestKit(actorSystem) {
            {
                Props props = JobExecutionMonitor.getProps(MessageForwarderActor.getProps(getRef()),
                                                           new FiniteDuration(10, TimeUnit.MILLISECONDS));
                ActorRef testActor = getSystem().actorOf(props);

                watch(testActor);
                testActor.tell(message, getRef());

                expectMsgClass(ExecuteNewJobMessage.class);

                JobProgressMessage progressMessage =
                        expectMsgClass(Duration.create(1, TimeUnit.SECONDS), JobProgressMessage.class);

                assertEquals(job.getId(), progressMessage.getJob().getId());
                assertEquals(ExecutionStatus.AWAITING_CHILD_COMPLETION, progressMessage.getJob().getStatus());
                assertEquals(1, progressMessage.getJob().getPendingChildren().size());

                expectTerminated(Duration.create(1, TimeUnit.SECONDS), testActor);

                shutdown(getSystem());
            }
        };
    }
}