package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.base.Joiner;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * Copyright RedRock 2013-14
 */
final class JobExecutionMonitor extends UntypedActor {
    private static final FiniteDuration PROGRESS_UPDATE_PERIOD  = Duration.create(350, TimeUnit.MILLISECONDS);
    private static final String         PROGRESS_UPDATE_MESSAGE = "progressUpdate";

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Props          jobExecutorProps;
    private final FiniteDuration progressUpdateInterval;

    private ActorRef            jobExecutionRequester;
    private JobExecutionContext jobExecutionContext;

    public JobExecutionMonitor(Props jobExecutorProps, FiniteDuration progressUpdateInterval) {
        this.jobExecutorProps = jobExecutorProps;
        this.progressUpdateInterval = progressUpdateInterval;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ExecuteJobMessage) {
            startJobExecutor((ExecuteJobMessage) message);
            scheduleNextProgressUpdate();
        }
        else if (message.equals(JobExecutionMonitor.PROGRESS_UPDATE_MESSAGE)) {
            updateProgress();
        }
        else {
            unhandled(message);
        }
    }

    private void startJobExecutor(ExecuteJobMessage message) {
        log.info("Starting job executor for: " + message.getJobExecutionContext().getJob());

        jobExecutionRequester = getSender();
        jobExecutionContext = message.getJobExecutionContext();

        ActorRef jobExecutor = getContext().actorOf(jobExecutorProps);
        jobExecutor.tell(message, getSelf());
    }

    private void scheduleNextProgressUpdate() {
        getContext().system()
                    .scheduler()
                    .scheduleOnce(progressUpdateInterval,
                                  getSelf(),
                                  JobExecutionMonitor.PROGRESS_UPDATE_MESSAGE,
                                  getContext().dispatcher(),
                                  null);
    }

    private void updateProgress() {
        JobProgressMessage progressMessage = new JobProgressMessage(jobExecutionContext);
        jobExecutionRequester.tell(progressMessage, getSelf());

        log.warning("Progress update for {} with state = {} and pending children = {}",
                    progressMessage.getJob(),
                    progressMessage.getJob().getStatus(),
                    Joiner.on(", ").join(progressMessage.getJob().getPendingChildren()));

        if (progressMessage.getJob().getStatus() != ExecutionStatus.RUNNING) {
            log.info("Shutting down executor for {} with state {}",
                     jobExecutionContext.getJob(),
                     jobExecutionContext.getJob().getStatus());
            context().system().stop(getSelf());
        }
        else {
            scheduleNextProgressUpdate();
        }
    }

    public static Props getProps() {
        return JobExecutionMonitor.getProps(JobExecutor.getProps(), JobExecutionMonitor.PROGRESS_UPDATE_PERIOD);
    }

    public static Props getProps(Props jobExecutorProps, FiniteDuration progressUpdateInterval) {
        return Props.create(JobExecutionMonitor.class,
                            () -> new JobExecutionMonitor(jobExecutorProps, progressUpdateInterval));
    }
}
