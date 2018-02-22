package com.redrock.jade.cloudMama.jobs;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Copyright RedRock 2013-14
 */
public final class MessageForwarderActor extends UntypedActor {
    private final ActorRef forwardingTarget;

    public MessageForwarderActor(ActorRef forwardingTarget) {
        this.forwardingTarget = forwardingTarget;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        forwardingTarget.tell(o, getSender());
    }

    public static Props getProps(ActorRef forwardingTarget) {
        return Props.create(MessageForwarderActor.class, () -> new MessageForwarderActor(forwardingTarget));
    }
}
