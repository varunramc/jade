package com.redrock.jade.cloudMama.services;

/**
 * Copyright RedRock 2013-14
 */
public interface ServiceLocatorClient {
    Iterable<Service> getServices(String type);

    Service getService(String type, String ownerId);
}
