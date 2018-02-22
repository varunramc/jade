package com.redrock.jade.cloudMama.jobs;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Copyright RedRock 2013-14
 */
final class JobExecutor extends UntypedActor {
    private final LoggingAdapter log;

    public JobExecutor() {
        log = Logging.getLogger(getContext().system(), this);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ExecuteNewJobMessage) {
            executeNewJob((ExecuteNewJobMessage) message);
        }
        else if (message instanceof ExecuteJobOnChildrenCompletionMessage) {
            executeJobOnChildrenCompletion((ExecuteJobOnChildrenCompletionMessage) message);
        }
        else {
            unhandled(message);
        }
    }

    public static Props getProps() {
        return Props.create(JobExecutor.class, JobExecutor::new);
    }

    protected void executeNewJob(ExecuteNewJobMessage message) {
        JobExecutionContext executionContext = message.getJobExecutionContext();
        Job job = executionContext.getJob();
        JobEnvironment environment = executionContext.getEnvironment();

        job.startJob(environment);
    }

    protected void executeJobOnChildrenCompletion(ExecuteJobOnChildrenCompletionMessage message) {
        JobExecutionContext executionContext = message.getJobExecutionContext();
        Job job = executionContext.getJob();
        JobEnvironment environment = executionContext.getEnvironment();

        job.onChildrenCompletion(message.getChildren(), environment);
    }
}
