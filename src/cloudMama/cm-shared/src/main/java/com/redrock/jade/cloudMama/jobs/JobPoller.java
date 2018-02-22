package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.redrock.jade.cloudMama.StreamUtils;
import com.redrock.jade.shared.dao.DocumentStore;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Copyright RedRock 2013-14
 */
public final class JobPoller extends UntypedActor {

    static final         FiniteDuration DEFAULT_POLL_DURATION = Duration.create(1, TimeUnit.SECONDS);
    private static final String         POLL_MESSAGE          = "poll";

    private final LoggingAdapter log;
    private final DocumentStore  documentStore;
    private final Props          jobDispatcherProps;
    private final FiniteDuration pollDuration;

    public JobPoller(DocumentStore documentStore, Props jobDispatcherProps, FiniteDuration pollDuration) {
        log = Logging.getLogger(getContext().system(), this);
        this.documentStore = documentStore;
        this.jobDispatcherProps = jobDispatcherProps;
        this.pollDuration = pollDuration;
    }

    @Override
    public void preStart() throws Exception {
        scheduleNextJobPoll();
    }

    private void scheduleNextJobPoll() {
        getContext().system()
                    .scheduler()
                    .scheduleOnce(pollDuration,
                                  getSelf(),
                                  JobPoller.POLL_MESSAGE,
                                  getContext().dispatcher(),
                                  null);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message.equals(JobPoller.POLL_MESSAGE)) {
            pollForJobsToExecute();
            scheduleNextJobPoll();
        }
        else {
            unhandled(message);
        }
    }

    private void pollForJobsToExecute() {
        JobCollection jobCollection = new JobCollection(documentStore);
        Iterable<Job> pendingJobs = jobCollection.getJobsPendingExecution();

        for (Job job : pendingJobs) {
            if (job.getStatus() == ExecutionStatus.AWAITING_CHILD_COMPLETION) {
                handleWaitingJob(jobCollection, job);
            }
            else {
                handleNewJob(jobCollection, job);
            }
        }
    }

    private void handleWaitingJob(JobCollection jobCollection, Job job) {
        List<Job> childJobs = StreamUtils.stream(job.getPendingChildren())
                                         .map(jobCollection::getById)
                                         .filter(child -> child != null && child.getStatus().isComplete())
                                         .collect(Collectors.toList());

        if (childJobs.isEmpty()) {
            return;
        }

        log.info("Found waiting job ready to execute: " + job.getId() + ". Completed child count: " + childJobs.size() + "/// DbID = " + job.dbId);
        log.info(">>> Children complete for {}: {}", job.dbId, Joiner.on(", ").join(childJobs));
        setJobDispatchingState(jobCollection, job);
        ActorRef jobExecutorActor = context().actorOf(jobDispatcherProps);
        jobExecutorActor.tell(new ResumeJobOnChildrenCompletionMessage(job, childJobs), getSelf());
    }

    private void handleNewJob(JobCollection jobCollection, Job job) {
        log.info("Found new job ready to execute: " + job.getId());

        setJobDispatchingState(jobCollection, job);
        ActorRef jobExecutorActor = context().actorOf(jobDispatcherProps);
        jobExecutorActor.tell(new StartJobMessage(job), getSelf());
    }

    private void setJobDispatchingState(JobCollection jobCollection, Job job) {
        log.error(".................. Changed dispatch status of " + job.dbId + " whose status = " + job.getStatus());
        job.setStatus(ExecutionStatus.DISPATCHING);
        jobCollection.save(job);

        if (jobCollection.getById(job.getId()).getStatus() != ExecutionStatus.DISPATCHING) {
            log.warning("-----------------------> Failed to save state");
        }
    }

    public static Props getProps(DocumentStore documentStore, Props jobDispatcherProps, FiniteDuration pollDuration) {
        return Props.create(JobPoller.class, () -> new JobPoller(documentStore, jobDispatcherProps, pollDuration));
    }
}
