package com.redrock.jade.cloudMama.services;

import com.redrock.jade.cloudMama.HostInstance;
import com.redrock.jade.shared.dao.DocumentException;
import com.redrock.jade.shared.dao.DocumentReference;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class HostService extends Service {
    private DocumentReference<HostInstance> hostInstance;

    public HostService() {
    }

    public HostService(HostInstance hostInstance, String type, String address, int port) {
        super(type, address, port);

        this.hostInstance = new DocumentReference<>(hostInstance);
    }

    @Override
    public void resolveReferences(DocumentStore documentStore) throws DocumentException {
        super.resolveReferences(documentStore);

        hostInstance.resolve(documentStore, HostInstance.class);
    }
}
