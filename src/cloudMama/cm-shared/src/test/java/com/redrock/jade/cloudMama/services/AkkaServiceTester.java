package com.redrock.jade.cloudMama.services;

import akka.actor.ActorSystem;

/**
 * Copyright RedRock 2013-14
 */
public final class AkkaServiceTester extends AkkaRemoteService {
    private static final String ServiceType = "AkkaServiceTester";

    public AkkaServiceTester() {
        super(AkkaServiceTester.ServiceType);
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }
}
