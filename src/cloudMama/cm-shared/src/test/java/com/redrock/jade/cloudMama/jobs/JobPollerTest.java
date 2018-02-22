package com.redrock.jade.cloudMama.jobs;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import akka.testkit.JavaTestKit;
import com.google.common.base.Strings;
import com.redrock.jade.cloudMama.FongoDocumentStore;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.shared.dao.Document;
import com.redrock.jade.shared.dao.DocumentStore;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public final class JobPollerTest {
    private DocumentStore documentStore;

    @Before
    public void setUp() throws Exception {
        documentStore = new FongoDocumentStore();
    }

    @Test
    public void actor_oneNewJobAvailable_jobPostedForDispatch() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        Job newJob = new JobStub();
        Job runningJob = new JobStub();
        runningJob.setStatus(ExecutionStatus.RUNNING);

        JobCollection jobCollection = new JobCollection(documentStore);
        jobCollection.insert(newJob);
        jobCollection.insert(runningJob);


        new JavaTestKit(actorSystem) {
            {
                ActorRef testActor =
                        getSystem().actorOf(JobPoller.getProps(documentStore, MessageForwarderActor.getProps(getRef()),
                                                               FiniteDuration.Zero()));

                watch(testActor);
                StartJobMessage msg = expectMsgClass(StartJobMessage.class);
                assertEquals(newJob.getId(), msg.getJob().getId());
                assertEquals(ExecutionStatus.DISPATCHING, msg.getJob().getStatus());

                shutdown(getSystem());
            }
        };
    }

    @Test
    public void actor_oneJobToResume_jobAndCompletedChildrenPostedForDispatch() throws Exception {
        ActorSystem actorSystem = ActorSystem.apply();

        Job job = new JobStub();
        Job child1 = new JobStub();
        Job child2 = new JobStub();
        Job child3 = new JobStub();

        job.startChildJob(child1);
        job.startChildJob(child2);
        job.startChildJob(child3);

        job.setStatus(ExecutionStatus.AWAITING_CHILD_COMPLETION);
        child1.setStatus(ExecutionStatus.FAILED);
        child2.setStatus(ExecutionStatus.RUNNING);
        child3.setStatus(ExecutionStatus.SUCCEEDED);

        JobCollection jobCollection = new JobCollection(documentStore);
        jobCollection.insert(Arrays.asList(job, child1, child2, child3));

        new JavaTestKit(actorSystem) {
            {
                ActorRef testActor =
                        getSystem().actorOf(JobPoller.getProps(documentStore, MessageForwarderActor.getProps(getRef()),
                                                               FiniteDuration.Zero()));

                watch(testActor);
                ResumeJobOnChildrenCompletionMessage msg = expectMsgClass(ResumeJobOnChildrenCompletionMessage.class);

                assertEquals(job.getId(), msg.getJob().getId());
                assertEquals(ExecutionStatus.DISPATCHING, jobCollection.getById(job.getId()).getStatus());

                List<String> children = StreamUtils.stream(msg.getCompletedChildren()).map(Document::getId).collect(
                        Collectors.toList());
                assertEquals(2, children.size());
                assertTrue(children.contains(child1.getId()));
                assertTrue(children.contains(child3.getId()));

                shutdown(getSystem());
            }
        };
    }
}