package com.redrock.jade.cloudMama.services;

import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Option;

/**
 * Copyright RedRock 2013-14
 */
public abstract class AkkaRemoteService {
    protected final String serviceType;
    protected       ActorSystem actorSystem;

    protected AkkaRemoteService(String serviceType) {
        this.serviceType = serviceType;
    }

    public void start() {
        Config config = ConfigFactory.load()
                                     .getConfig(serviceType);
        actorSystem = ActorSystem.create(serviceType, config);
        actorSystem.log()
                   .info("Akka remote service '{}' has started on '{}:{}'",
                         serviceType,
                         getHostname(),
                         getPort());
    }

    public void stop() {
        if (actorSystem != null) {
            actorSystem.shutdown();
            actorSystem.awaitTermination();
            actorSystem = null;
        }
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getPort() {
        Option<Object> portOption = actorSystem.provider().getDefaultAddress().port();

        if (portOption.isEmpty()) {
            throw new IllegalStateException("No port is assigned to the actor system: " + serviceType);
        }

        return (int)portOption.get();
    }

    public String getHostname() {
        Option<String> hostOption = actorSystem.provider().getDefaultAddress().host();

        if (hostOption.isEmpty()) {
            throw new IllegalStateException("No hostname is assigned to the actor system: " + serviceType);
        }

        return hostOption.get();
    }
}
