package com.redrock.jade.cloudMama.services;

import com.redrock.jade.cloudMama.RoleInstance;
import com.redrock.jade.shared.dao.DocumentException;
import com.redrock.jade.shared.dao.DocumentReference;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class RoleService extends Service {
    private DocumentReference<RoleInstance> roleInstance;

    public RoleService() {
    }

    public RoleService(RoleInstance roleInstance, String type, String address, int port) {
        super(type, address, port);

        this.roleInstance = new DocumentReference<>(roleInstance);
    }

    @Override
    public void resolveReferences(DocumentStore documentStore) throws DocumentException {
        super.resolveReferences(documentStore);

        roleInstance.resolve(documentStore, RoleInstance.class);
    }
}
