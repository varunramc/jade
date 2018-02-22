package com.redrock.jade.cloudMama.masterd.locator;

import akka.routing.RoundRobinPool;
import com.redrock.jade.shared.dao.DocumentStore;
import com.redrock.jade.cloudMama.services.AkkaRemoteService;

/**
 * Copyright RedRock 2013-14
 */
public final class LocatorService extends AkkaRemoteService {
    public static final String ServiceType = "ServiceLocator";
    public static final int LISTENER_POOL_SIZE = 5;

    private final DocumentStore documentStore;

    public LocatorService(DocumentStore documentStore) {
        super(LocatorService.ServiceType);
        this.documentStore = documentStore;
    }

    @Override
    public void start() {
        super.start();

        actorSystem.actorOf(new RoundRobinPool(LocatorService.LISTENER_POOL_SIZE).props(LocatorWorker.getProps(
                documentStore)));
    }
}
