package com.redrock.jade.cloudMama.services.locator;

import akka.actor.ActorRef;

/**
 * Copyright RedRock 2013-14
 */
public interface LocatorEndpointProvider {
    ActorRef getEndpoint();
}
