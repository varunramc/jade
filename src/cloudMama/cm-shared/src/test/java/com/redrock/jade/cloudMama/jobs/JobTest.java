package com.redrock.jade.cloudMama.jobs;

import com.google.common.base.Strings;
import com.redrock.jade.cloudMama.jobs.exceptions.CallbackMethodException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class JobTest {
    @Test(expected = NullPointerException.class)
    public void setParent_Null_Throws() throws Exception {
        Job job = new JobStub();
        job.setParent(null);
    }

    @Test
    public void setParent_validParent_correctParentAndRoot() throws Exception {
        Job root = new JobStub();
        Job parent = new JobStub();
        Job child = new JobStub();

        parent.setParent(root);
        child.setParent(parent);

        assertEquals(null, root.getParentId());
        assertEquals(root.getId(), root.getRootJobId());

        assertEquals(root.getId(), parent.getParentId());
        assertEquals(root.getId(), parent.getRootJobId());

        assertEquals(parent.getId(), child.getParentId());
        assertEquals(root.getId(), child.getRootJobId());
    }

    @Test
    public void resetAndGetChildJobsToStart_singleChild_returnsChildAndResets() throws Exception {
        Job job = new JobStub((j, env) -> j.startChildJob(new JobStub()));

        job.start();
        Collection<Job> childJobsToStart = job.resetAndGetChildJobsToStart();
        assertEquals(1, childJobsToStart.size());

        assertTrue(job.resetAndGetChildJobsToStart().isEmpty());
    }

    @Test
    public void startJob_simpleStart_jobCompletes() throws Exception {
        final boolean[] startWasCalled = {false};

        JobStub job = new JobStub((j, env) -> {
            startWasCalled[0] = true;
            env.log().logError("Something happened");
        });
        JobEnvironment environment = new JobEnvironment();

        job.startJob(environment);

        assertTrue(startWasCalled[0]);
        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
        assertEquals(1f, job.getProgressPercentage(), 0f);
        assertEquals(1, environment.log().getLogEntries().size());
    }

    @Test
    public void startJob_childStarted_awaitsChild() throws Exception {
        Job child = new JobStub();
        JobStub job = new JobStub((j, env) -> j.startChildJob(child));

        job.startJob(new JobEnvironment());

        assertEquals(ExecutionStatus.AWAITING_CHILD_COMPLETION, job.getStatus());

        List<Job> childrenToStart = job.resetAndGetChildJobsToStart();
        assertEquals(1, childrenToStart.size());
        assertEquals(child.getId(), childrenToStart.get(0).getId());

        assertEquals(1, job.getChildren().size());
        assertEquals(child.getId(), job.getChildren().stream().findFirst().get());

        assertEquals(1, job.getPendingChildren().size());
        assertEquals(child.getId(), job.getPendingChildren().stream().findFirst().get());
    }

    @Test
    public void startJob_exceptionThrown_exceptionCaughtAndJobFails() throws Exception {
        Job job = new JobStub((j, env) -> {
            throw new RuntimeException();
        });

        JobEnvironment environment = new JobEnvironment();
        job.startJob(environment);

        assertEquals(ExecutionStatus.FAILED, job.getStatus());
        assertFalse(Strings.isNullOrEmpty(job.getError()));
        assertEquals(1, environment.log().getLogEntries().size());
        assertEquals(LogLevel.ERROR, environment.log().getLogEntries().iterator().next().getLevel());
    }

    @Test
    public void onChildCompletion_lastChild_jobCompletes() throws Exception {
        Job child = new JobStub();
        JobStub job = new JobStub();

        child.setStatus(ExecutionStatus.SUCCEEDED);
        child.setProgressPercentage(1f);

        job.startChildJob(child, JobStub::childCompletionHandler);
        job.onChildrenCompletion(Arrays.asList(child), new JobEnvironment());

        assertTrue(job.onCompleteWasCalled);
        assertEquals(1f, job.completedChild.getProgressPercentage(), 0f);
        assertEquals(ExecutionStatus.SUCCEEDED, job.completedChild.getStatus());
        assertTrue(job.getPendingChildren().isEmpty());
        assertEquals(ExecutionStatus.SUCCEEDED, job.getStatus());
        assertTrue(Strings.isNullOrEmpty(job.getError()));
    }

    @Test
    public void onChildCompletion_childrenRemaining_handlerCalledAndAwaitsChildren() throws Exception {
        Job child1 = new JobStub();
        Job child2 = new JobStub();
        Job child3 = new JobStub();
        JobStub job = new JobStub();

        child1.setStatus(ExecutionStatus.SUCCEEDED);
        child1.setProgressPercentage(1f);

        job.startChildJob(child1, JobStub::childCompletionHandler);
        job.startChildJob(child2);
        job.startChildJob(child3);
        job.onChildrenCompletion(Arrays.asList(child1, child2), new JobEnvironment());

        assertTrue(job.onCompleteWasCalled);
        assertEquals(child1.getId(), job.completedChild.getId());
        assertEquals(1f, job.completedChild.getProgressPercentage(), 0f);
        assertEquals(ExecutionStatus.SUCCEEDED, job.completedChild.getStatus());

        assertFalse(job.getPendingChildren().isEmpty());
        assertEquals(ExecutionStatus.AWAITING_CHILD_COMPLETION, job.getStatus());
        assertTrue(Strings.isNullOrEmpty(job.getError()));
    }

    @Test
    public void onChildCompletion_handlerThrows_errorSavedAndJobFails() throws Exception {
        Job child = new JobStub();
        JobStub job = new JobStub();

        JobEnvironment environment = new JobEnvironment();
        job.startChildJob(child, JobStub::childCompletionHandlerThatThrows);
        job.onChildrenCompletion(Arrays.asList(child), environment);

        assertTrue(job.onCompleteWasCalled);

        assertEquals(ExecutionStatus.FAILED, job.getStatus());
        assertFalse(Strings.isNullOrEmpty(job.getError()));

        assertEquals(1, environment.log().getLogEntries().size());
        assertEquals(LogLevel.ERROR, environment.log().getLogEntries().iterator().next().getLevel());
    }

    @Test
    public void setProgressPercentage_validValues_succeeds() throws Exception {
        Job job = new JobStub();
        Consumer<Float> testPercentage = progress -> {
            job.setProgressPercentage(progress);
            assertEquals(progress, job.getProgressPercentage(), 0f);
        };

        testPercentage.accept(0f);
        testPercentage.accept(0.5f);
        testPercentage.accept(1f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setProgressPercentage_invalidLargeValue_throws() throws Exception {
        Job job = new JobStub();
        job.setProgressPercentage(1.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setProgressPercentage_invalidNegativeValue_throws() throws Exception {
        Job job = new JobStub();
        job.setProgressPercentage(-0.5f);
    }

    @Test(expected = CallbackMethodException.class)
    public void startChildJob_finalClass_throws() throws Exception {
        Job job = new FinalJobStub();
        job.start();
    }

    @Test(expected = CallbackMethodException.class)
    public void startChildJob_nonPublicClass_throws() throws Exception {
        Job job = new NonPublicJobStub();
        job.start();
    }

    @Test(expected = CallbackMethodException.class)
    public void startChildJob_noPublicEmptyConstructor_throws() throws Exception {
        Job job = new NoPublicEmptyConstructorJobStub(4);
        job.start();
    }
}