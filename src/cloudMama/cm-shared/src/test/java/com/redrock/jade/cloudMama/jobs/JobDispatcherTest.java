package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.redrock.jade.cloudMama.FongoDocumentStore;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.shared.dao.Document;
import com.redrock.jade.shared.dao.DocumentStore;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class JobDispatcherTest {
    private DocumentStore documentStore;

    @Before
    public void setUp() throws Exception {
        documentStore = new FongoDocumentStore();
    }

    @Test
    public void actor_startJobMessage_messageDispatched() throws Exception {

        StartJobMessage msg = new StartJobMessage(new JobStub());
        StartJobMessage receivedMsg = postMessageToDispatcher(msg, StartJobMessage.class);

        assertEquals(msg.getJob().getId(), receivedMsg.getJob().getId());
    }

    @Test
    public void actor_resumeJobMessage_messageDispatched() throws Exception {

        List<Job> completedChildren = Arrays.asList(new JobStub(), new JobStub());
        ResumeJobOnChildrenCompletionMessage msg = new ResumeJobOnChildrenCompletionMessage(new JobStub(),
                                                                                            completedChildren);
        ResumeJobOnChildrenCompletionMessage receivedMsg =
                postMessageToDispatcher(msg, ResumeJobOnChildrenCompletionMessage.class);

        assertEquals(msg.getJob().getId(), receivedMsg.getJob().getId());
        assertArrayEquals(completedChildren.stream().map(Document::getId).sorted().toArray(),
                          StreamUtils.stream(receivedMsg.getCompletedChildren()).map(
                                  Document::getId).sorted().toArray());
    }

    private <T extends JobMessage> T postMessageToDispatcher(T msg, Class<T> msgClass) {
        ActorSystem actorSystem = ActorSystem.apply();
        JavaTestKit testKit = new JavaTestKit(actorSystem);
        Props dispatcherProps = JobDispatcher.getProps(documentStore,
                                                       job -> testKit.getSystem()
                                                                     .actorSelection(testKit.getRef()
                                                                                            .path()));
        ActorRef testActor = testKit.getSystem().actorOf(dispatcherProps);

        testKit.watch(testActor);
        testActor.tell(msg, testKit.getRef());

        T receivedMsg = testKit.expectMsgClass(msgClass);

        testKit.shutdown(testKit.getSystem());
        return receivedMsg;
    }

    @Test
    public void actor_progressMessageForUnfinishedJob_stateLogsAndChildrenSaved() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        Job job = new JobStub();
        Job child = new JobStub();

        job.setStatus(ExecutionStatus.RUNNING);
        job.startChildJob(child);

        JobEnvironment jobEnvironment = new JobEnvironment();
        jobEnvironment.log().logError("Some error");
        JobExecutionContext jobContext = new JobExecutionContext(job, jobEnvironment);
        new JavaTestKit(actorSystem) {
            {
                Props dispatcherProps =
                        JobDispatcher.getProps(documentStore, job -> getSystem().actorSelection(getRef().path()));
                ActorRef testActor = getSystem().actorOf(dispatcherProps);

                watch(testActor);
                JobProgressMessage msg = new JobProgressMessage(jobContext);

                testActor.tell(msg, getRef());
                expectNoMsg(new FiniteDuration(1, TimeUnit.SECONDS));
                shutdown(getSystem());
            }
        };

        JobCollection jobCollection = new JobCollection(documentStore);
        JobLogCollection logsCollection = new JobLogCollection(documentStore);

        assertEquals(job.getStatus(), jobCollection.getById(job.getId()).getStatus());
        assertEquals(job.getId(), jobCollection.getById(child.getId()).getParentId());

        Optional<JobLogEntry> firstLog = StreamUtils.stream(logsCollection.getLogsForJob(job.getId())).findFirst();
        assertTrue(firstLog.isPresent());
        assertEquals(LogLevel.ERROR, firstLog.get().getLevel());
    }

    @Test
    public void actor_progressMessageForCompletedJob_stateLogsSavedAndActorShutsDown() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        Job job = new JobStub();
        Job child = new JobStub();

        job.startChildJob(child);
        job.setStatus(ExecutionStatus.FAILED);

        JobEnvironment jobEnvironment = new JobEnvironment();
        jobEnvironment.log().logVerbose("Some message");
        JobExecutionContext jobContext = new JobExecutionContext(job, jobEnvironment);
        new JavaTestKit(actorSystem) {
            {
                Props dispatcherProps =
                        JobDispatcher.getProps(documentStore, job -> getSystem().actorSelection(getRef().path()));
                ActorRef testActor = getSystem().actorOf(dispatcherProps);

                watch(testActor);
                JobProgressMessage msg = new JobProgressMessage(jobContext);

                testActor.tell(msg, getRef());
                expectTerminated(new FiniteDuration(1, TimeUnit.SECONDS), testActor);
                shutdown(getSystem());
            }
        };

        JobCollection jobCollection = new JobCollection(documentStore);
        JobLogCollection logsCollection = new JobLogCollection(documentStore);

        assertEquals(job.getStatus(), jobCollection.getById(job.getId()).getStatus());
        assertTrue(StreamUtils.stream(jobCollection.getAll()).noneMatch(j -> j.getId().equals(child.getId())));

        Optional<JobLogEntry> firstLog = StreamUtils.stream(logsCollection.getLogsForJob(job.getId())).findFirst();
        assertTrue(firstLog.isPresent());
        assertEquals(LogLevel.VERBOSE, firstLog.get().getLevel());
    }
}