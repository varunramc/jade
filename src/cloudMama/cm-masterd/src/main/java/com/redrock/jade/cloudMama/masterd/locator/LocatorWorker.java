package com.redrock.jade.cloudMama.masterd.locator;

import akka.actor.Props;
import akka.actor.UntypedActor;
import com.redrock.jade.shared.dao.DocumentStore;
import com.redrock.jade.cloudMama.services.Service;
import com.redrock.jade.cloudMama.services.ServiceCollection;
import com.redrock.jade.cloudMama.services.messages.GetServicesMsg;

/**
 * Copyright RedRock 2013-14
 */
public final class LocatorWorker extends UntypedActor {

    private final DocumentStore documentStore;

    public LocatorWorker(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof GetServicesMsg) {
            getServices((GetServicesMsg) message);
        }
        else {
            unhandled(message);
        }
    }

    private void getServices(GetServicesMsg getServicesMsg) {
        Iterable<Service> services = new ServiceCollection(documentStore).getAllByType(getServicesMsg.getServiceType());
        getSender().tell(services, getSelf());
    }

    public static Props getProps(DocumentStore documentStore) {
        return Props.create(() -> new LocatorWorker(documentStore));
    }
}
