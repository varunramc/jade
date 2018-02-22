package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Copyright RedRock 2013-14
 */
final class JobExecutionRequestRouter extends UntypedActor {
    private final JobExecutionContextCreator contextCreator;

    public JobExecutionRequestRouter(JobExecutionContextCreator contextCreator) {
        this.contextCreator = contextCreator;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartJobMessage) {
            startNewJob((StartJobMessage) message);
        }
        else if (message instanceof ResumeJobOnChildrenCompletionMessage) {
            resumeJob((ResumeJobOnChildrenCompletionMessage)message);
        }
        else {
            unhandled(message);
        }
    }

    private void startNewJob(StartJobMessage message) {
        JobExecutionContext jobExecutionContext = contextCreator.create(message.getJob());
        ExecuteNewJobMessage executeNewJobMessage = new ExecuteNewJobMessage(jobExecutionContext);
        startJobExecutionMonitor(executeNewJobMessage);
    }

    private void resumeJob(ResumeJobOnChildrenCompletionMessage message) {
        JobExecutionContext jobExecutionContext = contextCreator.create(message.getJob());
        ExecuteJobOnChildrenCompletionMessage executeResumedJobMessage =
                new ExecuteJobOnChildrenCompletionMessage(jobExecutionContext, message.getCompletedChildren());
        startJobExecutionMonitor(executeResumedJobMessage);
    }

    private void startJobExecutionMonitor(ExecuteJobMessage message) {
        ActorRef jobMonitor = getContext().actorOf(JobExecutionMonitor.getProps());
        jobMonitor.tell(message, getSender());
    }

    public static Props getProps(JobExecutionContextCreator contextCreator) {
        return Props.create(JobExecutionRequestRouter.class, () -> new JobExecutionRequestRouter(contextCreator));
    }
}
