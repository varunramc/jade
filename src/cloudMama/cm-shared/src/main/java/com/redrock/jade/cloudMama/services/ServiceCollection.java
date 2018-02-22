package com.redrock.jade.cloudMama.services;

import com.redrock.jade.cloudMama.HostInstance;
import com.redrock.jade.cloudMama.RoleInstance;
import com.redrock.jade.shared.dao.DocumentCollection;
import com.redrock.jade.shared.dao.DocumentStore;

/**
 * Copyright RedRock 2013-14
 */
public final class ServiceCollection extends DocumentCollection<Service> {

    public ServiceCollection(DocumentStore documentStore) {
        super(documentStore, Service.class);
    }

    public Iterable<Service> getAllByType(String type) {
        return collection.find("{type: #}", type).as(documentClass);
    }

    public HostService getHostService(DocumentStore documentStore, String type, HostInstance hostInstance) {
        return collection.findOne("{type: #, hostInstance: {id: #}}", type, hostInstance.getId()).as(HostService.class);
    }

    public RoleService getRoleService(DocumentStore documentStore, String type, RoleInstance roleInstance) {
        return collection.findOne("{type: #, roleInstance: {id: #}}", type, roleInstance.getId()).as(RoleService.class);
    }
}
