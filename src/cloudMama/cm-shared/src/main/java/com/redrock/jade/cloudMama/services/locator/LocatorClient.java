package com.redrock.jade.cloudMama.services.locator;

import akka.actor.ActorRef;
import akka.dispatch.ExecutionContexts;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.redrock.jade.cloudMama.AkkaUtils;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.messages.GetServicesMsg;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Copyright RedRock 2013-14
 */
public final class LocatorClient {
    private static final Timeout ASK_TIMEOUT      = new Timeout(5, TimeUnit.SECONDS);
    public static final  int     EXECUTOR_THREADS = 2;

    private final ActorRef locatorServiceActor;
    private final ExecutionContext executionContext;

    public LocatorClient(LocatorEndpointProvider endpointProvider) {
        locatorServiceActor = endpointProvider.getEndpoint();
        executionContext = ExecutionContexts.fromExecutor(Executors.newFixedThreadPool(LocatorClient.EXECUTOR_THREADS));
    }

    public Future<Iterable<Service>> getServices(String serviceType) {
        Future<Object> untypedFuture =
                Patterns.ask(locatorServiceActor, new GetServicesMsg(serviceType), LocatorClient.ASK_TIMEOUT);

        return AkkaUtils.mapToType(untypedFuture, executionContext);
    }
}
