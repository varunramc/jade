package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.redrock.jade.cloudMama.AkkaUtils;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.cloudMama.jobs.exceptions.JobExecutionException;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import com.redrock.jade.shared.dao.DocumentStore;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Copyright RedRock 2013-14
 */
final class JobDispatcher extends UntypedActor {
    private final LoggingAdapter                log;
    private final DocumentStore                 documentStore;
    private final Function<Job, ActorSelection> jobExecutorMapper;

    private JobDispatcher(DocumentStore documentStore, Function<Job, ActorSelection> jobExecutorMapper) {
        log = Logging.getLogger(getContext().system(), this);
        this.documentStore = documentStore;

        if (jobExecutorMapper != null) {
            this.jobExecutorMapper = jobExecutorMapper;
        }
        else {
            this.jobExecutorMapper = this::getJobExecutorActor;
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartJobMessage) {
            dispatchJob((StartJobMessage) message);
        }
        else if (message instanceof ResumeJobOnChildrenCompletionMessage) {
            dispatchJob((ResumeJobOnChildrenCompletionMessage) message);
        }
        else if (message instanceof JobProgressMessage) {
            handleJobProgressMessage((JobProgressMessage) message);
        }
        else {
            unhandled(message);
        }
    }

    private void dispatchJob(JobMessage message) {
        ActorSelection jobExecutor = jobExecutorMapper.apply(message.getJob());
        log.info("Dispatching job '{}' for execution on '{}'", message.getJob(), jobExecutor);
        jobExecutor.tell(message, getSelf());
    }

    public static Props getProps(DocumentStore documentStore) {
        return JobDispatcher.getProps(documentStore, null);
    }

    public static Props getProps(DocumentStore documentStore, Function<Job, ActorSelection> jobExecutorMapper) {
        return Props.create(JobDispatcher.class, () -> new JobDispatcher(documentStore, jobExecutorMapper));
    }

    private ActorSelection getJobExecutorActor(Job job) {
        Service targetExecutorService = job.getExecutor(new ServiceCollection(documentStore));
        if (targetExecutorService == null) {
            job.setError("getExecutor() returned null");
            job.setStatus(ExecutionStatus.FAILED);
            saveJobState(job);

            throw new JobExecutionException(String.format("Job %s returned Null for target executor", job.getId()));
        }

        return context().actorSelection(
                AkkaUtils.getRemoteServiceActorPath(
                        targetExecutorService,
                        JobExecutorService.REQUEST_ROUTER_ACTOR_NAME)
        );
    }

    private void handleJobProgressMessage(JobProgressMessage progressMessage) {
        Job job = progressMessage.getJob();

        saveJobLogs(progressMessage);
        saveJobState(job);

        if (job.getStatus().isComplete()) {
            handleJobCompletion(job);
        }
        else {
            progressMessage.getChildJobsToStart().forEach(this::saveJobState);
        }
    }

    private void saveJobLogs(JobProgressMessage progressMessage) {
        Stream<JobLogEntry> jobLogEntryStream =
                StreamUtils.stream(progressMessage.getLogEntries())
                      .map(logEntry -> new JobLogEntry(progressMessage.getJob(),
                                                       logEntry.getLevel(),
                                                       logEntry.getMessage()));

        new JobLogCollection(documentStore).insert(jobLogEntryStream::iterator);
    }

    private void handleJobCompletion(Job job) {
        log.info("Job '{}' has finished execution with status '{}'", job, job.getStatus());
        getContext().stop(getSelf());
    }

    private void saveJobState(Job job) {
        new JobCollection(documentStore).save(job);
    }
}
